/*
 * Copyright (C) Inria Sophia Antipolis - Méditerranée / LIRMM
 * (Université de Montpellier & CNRS) (2014 - 2015)
 *
 * Contributors :
 *
 * Clément SIPIETER <clement.sipieter@inria.fr>
 * Mélanie KÖNIG
 * Swan ROCHER
 * Jean-François BAGET
 * Michel LECLÈRE
 * Marie-Laure MUGNIER <mugnier@lirmm.fr>
 *
 *
 * This file is part of Graal <https://graphik-team.github.io/graal/>.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
 /**
 * 
 */
package fr.lirmm.graphik.graal.io.dlp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lirmm.graphik.dlgp2.parser.DLGP2Parser;
import fr.lirmm.graphik.dlgp2.parser.ParseException;
import fr.lirmm.graphik.dlgp2.parser.TermFactory;
import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.KnowledgeBase;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.VariableGenerator;
import fr.lirmm.graphik.graal.api.io.ParseError;
import fr.lirmm.graphik.graal.api.io.Parser;
import fr.lirmm.graphik.graal.core.DefaultNegativeConstraint;
import fr.lirmm.graphik.graal.core.DefaultVariableGenerator;
import fr.lirmm.graphik.graal.core.FreshVarSubstitution;
import fr.lirmm.graphik.graal.core.stream.filter.AtomFilterIterator;
import fr.lirmm.graphik.graal.core.term.DefaultTermFactory;
import fr.lirmm.graphik.util.DefaultURI;
import fr.lirmm.graphik.util.Prefix;
import fr.lirmm.graphik.util.URI;
import fr.lirmm.graphik.util.stream.AbstractCloseableIterator;
import fr.lirmm.graphik.util.stream.ArrayBlockingStream;
import fr.lirmm.graphik.util.stream.GIterator;

/**
 * 
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 * 
 */
public final class DlgpParser extends AbstractCloseableIterator<Object> implements Parser<Object> {
	
	private static final Logger LOGGER = LoggerFactory
			.getLogger(DlgpParser.class);

	private static VariableGenerator    freeVarGen = new DefaultVariableGenerator("I");

	private ArrayBlockingStream<Object> buffer = new ArrayBlockingStream<Object>(
			512);

	private static class DlgpListener extends AbstractDlgpListener {

		private ArrayBlockingStream<Object> set;

		DlgpListener(ArrayBlockingStream<Object> buffer) {
			this.set = buffer;
		}

		@Override
		protected void createAtomSet(InMemoryAtomSet atomset) {
			FreshVarSubstitution s = new FreshVarSubstitution(freeVarGen);
			for (Atom a : atomset) {
				this.set.write(s.createImageOf(a));
			}
		}

		@Override
		protected void createQuery(ConjunctiveQuery query) {
			this.set.write(query);
		}

		@Override
		protected void createRule(Rule rule) {
			this.set.write(rule);
		}

		@Override
		protected void createNegConstraint(DefaultNegativeConstraint negativeConstraint) {
			this.set.write(negativeConstraint);
		}

		@Override
		public void declarePrefix(String prefix, String ns) {
			this.set.write(new Prefix(prefix.substring(0, prefix.length() - 1),
					ns));
		}

		@Override
		public void declareBase(String base) {
			this.set.write(new Directive(Directive.Type.BASE, base));
		}

		@Override
		public void declareTop(String top) {
			this.set.write(new Directive(Directive.Type.TOP, top));
		}

		@Override
		public void declareUNA() {
			this.set.write(new Directive(Directive.Type.UNA, ""));
		}

		@Override
		public void directive(String text) {
			this.set.write(new Directive(Directive.Type.COMMENT, text));
		}
	};

	private static class InternalTermFactory implements TermFactory {

		@Override
		public Object createIRI(String s) {
			if (s.indexOf(':') == -1) {
				return s;
			}
			return new DefaultURI(s);
		}

		@Override
		public Object createLiteral(Object datatype, String stringValue,
				String langTag) {
			if (langTag != null) {
				stringValue += "@" + langTag;
			}
			return DefaultTermFactory.instance().createLiteral((URI) datatype,
					stringValue);
		}

		@Override
		public Object createVariable(String stringValue) {
			return DefaultTermFactory.instance().createVariable(stringValue);
		}
	}

	private static class Producer implements Runnable {

		private Reader reader;
		private ArrayBlockingStream<Object> buffer;

		Producer(Reader reader, ArrayBlockingStream<Object> buffer) {
			this.reader = reader;
			this.buffer = buffer;
		}

		@Override
		public void run() {
			DLGP2Parser parser = new DLGP2Parser(new InternalTermFactory(), reader);
			parser.addParserListener(new DlgpListener(buffer));
			parser.setDefaultBase("");

			try {
				parser.document();
			} catch (ParseException e) {
				throw new ParseError("An error occured while parsing", e);
			} finally {
				buffer.close();
			}
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	// /////////////////////////////////////////////////////////////////////////
	
	private Reader reader = null;

	private UncaughtExceptionHandler exceptionHandler;

	/**
	 * Constructor for parsing from the given reader.
	 * @param reader
	 */
	public DlgpParser(Reader reader) {
		this(reader, null);
	}
	
	/**
	 * Constructor for parsing from the standard input.
	 */
	public DlgpParser() {
		this(new InputStreamReader(System.in));
	}
	
	/**
	 * Constructor for parsing from the given file.
	 * @param file
	 * @throws FileNotFoundException
	 */
	public DlgpParser(File file) throws FileNotFoundException {
		this(new FileReader(file));
	}

	/**
	 * Constructor for parsing the content of the string s as DLGP content.
	 * @param s
	 */
	public DlgpParser(String s) {
		this(new StringReader(s));
	}
	
	/**
	 * Constructor for parsing the given InputStream.
	 * @param in
	 */
	public DlgpParser(InputStream in) {
		this(new InputStreamReader(in));
	}
	
	/**
	 * Constructor for parsing from the given reader.
	 * 
	 * @param reader
	 */
	public DlgpParser(Reader reader, Thread.UncaughtExceptionHandler exceptionHandler) {
		this.reader = reader;
		Thread t = new Thread(new Producer(reader, buffer));
		t.setUncaughtExceptionHandler(exceptionHandler);
		t.start();
	}

	/**
	 * Constructor for parsing from the given file.
	 * 
	 * @param file
	 * @throws FileNotFoundException
	 */
	public DlgpParser(File file, Thread.UncaughtExceptionHandler exceptionHandler) throws FileNotFoundException {
		this(new FileReader(file), exceptionHandler);
	}

	/**
	 * Constructor for parsing the content of the string s as DLGP content.
	 * 
	 * @param s
	 */
	public DlgpParser(String s, Thread.UncaughtExceptionHandler exceptionHandler) {
		this(new StringReader(s), exceptionHandler);
	}

	/**
	 * Constructor for parsing the given InputStream.
	 * 
	 * @param in
	 */
	public DlgpParser(InputStream in, Thread.UncaughtExceptionHandler exceptionHandler) {
		this(new InputStreamReader(in), exceptionHandler);
	}

	@Override
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}

	// /////////////////////////////////////////////////////////////////////////
	// METHODS
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public boolean hasNext() {
		return buffer.hasNext();
	}

	@Override
	public Object next() {
		return buffer.next();
	}
	
	/**
	 * Closes the stream and releases any system resources associated with it.
	 * Closing a previously closed parser has no effect.
	 * 
	 * @throws IOException
	 */
	@Override
	public void close() {
		if(this.reader != null) {
			try {
				this.reader.close();
			} catch (IOException e) {
				LOGGER.error("Error during closing reader", e);
			}
			this.reader = null;
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// STATIC METHODS
	// /////////////////////////////////////////////////////////////////////////

	public static ConjunctiveQuery parseQuery(String s) {
		return (ConjunctiveQuery) new DlgpParser(s).next();
	}

	public static Atom parseAtom(String s) {
		return (Atom) new DlgpParser(s).next();
	}
	
	public static GIterator<Atom> parseAtomSet(String s) {
		return new AtomFilterIterator(new DlgpParser(s));
	}
	
	public static Rule parseRule(String s) {
		return (Rule) new DlgpParser(s).next();
	}
	
	public static DefaultNegativeConstraint parseNegativeConstraint(String s) {
		return (DefaultNegativeConstraint) new DlgpParser(s).next();
	}
	
	/**
	 * Parse a DLP content and store data into the KnowledgeBase target.
	 * 
	 * @param src
	 * @param target
	 * @throws AtomSetException 
	 */
	public static void parseKnowledgeBase(Reader src, KnowledgeBase target) throws AtomSetException {
		DlgpParser parser = new DlgpParser(src);
		while (parser.hasNext()) {
			Object o = parser.next();
			if (o instanceof Rule) {
				target.getOntology().add((Rule) o);
			} else if (o instanceof Atom) {
				target.getFacts().add((Atom) o);
			}
		}
		parser.close();
	}

};

/**
 * 
 */
package fr.lirmm.graphik.graal.io.rdf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;

import fr.lirmm.graphik.graal.ParseError;
import fr.lirmm.graphik.graal.core.Atom;
import fr.lirmm.graphik.graal.core.DefaultAtom;
import fr.lirmm.graphik.util.stream.AbstractReader;
import fr.lirmm.graphik.util.stream.ArrayBlockingStream;

/**
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 * 
 */
public final class RDFParser extends AbstractReader<Atom> {

	private ArrayBlockingStream<Atom> buffer = new ArrayBlockingStream<Atom>(
			512);

	private static class RDFListener extends AbstractRDFListener {

		private ArrayBlockingStream<Atom> set;

		public RDFListener(ArrayBlockingStream<Atom> set) {
			this.set = set;
		}

		@Override
		protected void createAtom(DefaultAtom atom) {
			this.set.write(atom);
		}
	};

	private static class Producer implements Runnable {

		private Reader reader;
		private ArrayBlockingStream<Atom> buffer;
		private RDFFormat format;

		Producer(Reader reader, ArrayBlockingStream<Atom> buffer, RDFFormat format) {
			this.reader = reader;
			this.buffer = buffer;
			this.format = format;
		}

		public void run() {

			org.openrdf.rio.RDFParser rdfParser = Rio
					.createParser(format);
			rdfParser.setRDFHandler(new RDFListener(buffer));
			try {
				rdfParser.parse(this.reader, "");
			} catch (RDFParseException e) {
				throw new ParseError("An error occured while parsing", e);
			} catch (RDFHandlerException e) {
				throw new ParseError("An error occured while parsing", e);
			} catch (IOException e) {
				throw new ParseError("An error occured while parsing", e);
			}
			buffer.close();

			try {
				this.reader.close();
			} catch (IOException e) {
			}

		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	// /////////////////////////////////////////////////////////////////////////

	public RDFParser(Reader reader, RDFFormat format) {
		new Thread(new Producer(reader, buffer, format)).start();
	}

	public RDFParser(URL url, RDFFormat format) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				url.openStream()));
		new Thread(new Producer(reader, buffer, format)).start();
	}

	public RDFParser(String s, RDFFormat format) {
		this(new StringReader(s), format);
	}

	// /////////////////////////////////////////////////////////////////////////
	// METHODS
	// /////////////////////////////////////////////////////////////////////////

	public boolean hasNext() {
		return buffer.hasNext();
	}

	public Atom next() {
		return buffer.next();
	}

};
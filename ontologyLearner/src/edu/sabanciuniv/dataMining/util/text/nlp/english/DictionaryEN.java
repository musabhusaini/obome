package edu.sabanciuniv.dataMining.util.text.nlp.english;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.nlp.ling.HasTag;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.WordTag;

import rita.wordnet.RiWordnet;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.dictionary.Dictionary;

public final class DictionaryEN {
	private static RiWordnet riWordnet;
	private static Dictionary wordnet;
	
	public static boolean ensureInitialized() {
		if (!JWNL.isInitialized()) {
			try {
				InputStream propsFile = DictionaryEN.class.getResourceAsStream("config/file_properties.xml");
				if (propsFile == null) {
					throw new FileNotFoundException();
				}
				
				JWNL.initialize(propsFile);
			} catch (FileNotFoundException e) {
				Logger.getLogger(DictionaryEN.class.getName()).log(Level.WARNING, "Could not find config file for WordNet.", e);
				return false;
			} catch (JWNLException e) {
				Logger.getLogger(DictionaryEN.class.getName()).log(Level.WARNING, "Could not initialize WordNet.", e);
				return false;
			}
		}
		
		if (DictionaryEN.riWordnet == null) {
			DictionaryEN.riWordnet = new RiWordnet(null, "c:\\program files\\wordnet\\2.1\\dict\\");
		}
		
		if (DictionaryEN.wordnet == null) {
			DictionaryEN.wordnet = Dictionary.getInstance();
		}
		
		return true;
	}
	
	public static POS getWordnetPos(String pennPos) {
		if (pennPos.startsWith("NN")) {
			return POS.NOUN;
		}  else if (pennPos.startsWith("VB")) {
			return POS.VERB;
		} else if (pennPos.startsWith("JJ")) {
			return POS.ADJECTIVE;
		} else if (pennPos.startsWith("RB")) {
			return POS.ADVERB;
		}
		
		return null;
	}
	
	public static String getRiWordnetPos(String pennPos) {
		if (pennPos != null) {
			if (pennPos.startsWith("NN")) {
				return RiWordnet.NOUN;
			}  else if (pennPos.startsWith("VB")) {
				return RiWordnet.VERB;
			} else if (pennPos.startsWith("JJ")) {
				return RiWordnet.ADJ;
			} else if (pennPos.startsWith("RB")) {
				return RiWordnet.ADV;
			}
		}
		
		return null;
	}
	
	private DictionaryEN() {
		DictionaryEN.ensureInitialized();
	}
	
	public static boolean wordExists(String word) {
		return DictionaryEN.wordExists(new WordTag(word, null));
	}
	
	public static <T extends HasWord & HasTag> boolean wordExists(T wt) {
		String pos = DictionaryEN.getRiWordnetPos(wt.tag());
		if (DictionaryEN.ensureInitialized()) {
			if (pos == null || pos.equals("")) {
				return DictionaryEN.riWordnet.exists(wt.word());
			} else {
				List<String> possiblePos = Arrays.asList(DictionaryEN.riWordnet.getPos(wt.word()));
				if (possiblePos != null && possiblePos.size() != 0) { 
					return possiblePos.contains(pos);
				}
			}
		}
		
		return true;
	}
	
	public static <T extends HasWord & HasTag> List<String> getSynonyms(T wt) {
		if (DictionaryEN.ensureInitialized()) {
			String[] syns = DictionaryEN.riWordnet.getSynonyms(wt.word(), DictionaryEN.getRiWordnetPos(wt.tag()));
			if (syns != null) {
				return Arrays.asList();
			}
		}
		
		return null;
	}
}
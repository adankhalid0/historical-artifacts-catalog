package no.khalidadan.funn.io;

import no.khalidadan.funn.model.Funngjenstand;
import no.khalidadan.funn.model.Museum;
import no.khalidadan.funn.model.Person;

import java.util.List;

/**
 * Everything FunnFileParser read out of funn.txt, bundled into one
 * immutable result instead of the parser needing three separate output
 * parameters or callers needing to call it three times.
 */
public record ParseResultat(List<Person> personer, List<Museum> museer, List<Funngjenstand> funn) {
}

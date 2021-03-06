package es.weso.utils

import es.weso.rdf.jena.RDFAsJenaModel
import es.weso.rdf.nodes.{BNode, IRI}
import es.weso.rdf.triples.RDFTriple
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._
import es.weso.utils.NormalizeBNodes._
import es.weso.utils.IOUtils._


class NormalizeBNodesTest extends AnyFunSpec with Matchers {
  describe(s"Parse RDF with blank nodes") {
    it(s"Should show RDF") {
      val str =
        """|prefix : <http://e.com/>
           |:x :p _:b1 .
           |_:b1 :q :x .
           |_:b1 :r :y .
           |_:b1 :t _:b2 .
           |_:b2 :u _:b1 .
        """.stripMargin

      def iri(x: String) = IRI(s"http://e.com/" + x)

      val r = for {
        empty <- RDFAsJenaModel.empty
        rdf1 <- RDFAsJenaModel.fromChars(str,"TURTLE",None)
        n1 <- normalizeBNodes(rdf1, empty)
        rdf2 <- RDFAsJenaModel.fromChars(str,"TURTLE", None)
        n2 <- normalizeBNodes(rdf2,empty)
        ss1 <- stream2io(n1.triplesWithSubject(BNode("0"))) // .right.value
        ss2 <- stream2io(n2.triplesWithSubject(BNode("0"))) // .right.value
      } yield (ss1,ss2)
      r.attempt.unsafeRunSync.fold(e => fail(s"Error: $e"), pair => {
        val (ss1,ss2) = pair
        val expected = List(
          RDFTriple(BNode("0"), iri("r"), iri("y")),
          RDFTriple(BNode("0"), iri("q"), iri("x")),
          RDFTriple(BNode("0"), iri("t"), BNode("1"))
      )
      ss1 should contain theSameElementsAs expected
      ss2 should contain theSameElementsAs expected
      }
      )

    }
  }


}
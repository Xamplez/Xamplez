package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import models._

class TagSpec extends Specification {
  
  "Tag" should {
    
    "parse tags" in {
      Tag.fetchTags("#toto.") must beEqualTo(List(Tag("toto")))
      Tag.fetchTags("ceci est une #phrase av#1.2.3ec. des #tags") must beEqualTo(List(Tag("phrase"), Tag("1.2.3ec"), Tag("tags")))
      Tag.fetchTags("#toto.titi") must beEqualTo(List(Tag("toto.titi")))
      Tag.fetchTags("#toto.#titi") must beEqualTo(List(Tag("toto"), Tag("titi")))
      Tag.fetchTags("#toto.#titi") must beEqualTo(List(Tag("toto"), Tag("titi")))
      Tag.fetchTags("#play1.2 #alpha") must beEqualTo(List(Tag("play1.2"), Tag("alpha")))
      Tag.fetchTags("#play1.2#alpha") must beEqualTo(List(Tag("play1.2"), Tag("alpha")))
      Tag.fetchTags("#play 1_2#alpha") must beEqualTo(List(Tag("play"), Tag("alpha")))
      Tag.fetchTags("#play1_2 #al2.pha") must beEqualTo(List(Tag("play1_2"), Tag("al2.pha")))
    }
    
  }
  
}
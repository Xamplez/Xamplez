package controllers

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.libs.functional.syntax._

import services.search.{Search => EsSearch}

object Gists extends Controller {

	def findById(id: Long) = Action {
		Async{
	    EsSearch.byId(id).map{ e =>
	    	e.fold(
	    		{ r => BadRequest("%s - %s".format(r.status, r.body) ) },
	    		{ json =>
	    			( json \ "hits" \ "hits").as[Seq[JsValue]] match {
	    				case head :: _ => Ok(head)
	    				case _ => NotFound(id.toString)
	    			} 
	    		}
	    	)
	    } recover {
	    	case e: Exception => BadRequest("Failed to load data : %s".format(e.getMessage))
	    }
	  }
	}

	def blacklist(id: Long) = Action {
	  Async{
	  	services.GistBlackList.add(id).map{ response =>
	  		response.status match {
	  			case 200 => Ok(s"Gist with $id blacklisted")
	  			case _ => BadRequest( response.toString )
	  		}
	  	}
	  }
	}

}
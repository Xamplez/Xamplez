## Welcome to Xamplez.io
#### Building an OpenSource Knowledge Cluster of Code Snippets

<br/>
### Xamplez.io's idea?

Xamplez is based on a very simple idea: 
> I want to share code snippets (and nothing else) about a given topic and be able to search them based 
> on tags, ratings etc...

Our primary goal is to **help people build code snippets bases, each focusing on a given topic**. The topic can go
from the very generic subject such as programming language or frameworks to the more specific such as 
very precise technical points. It's up to you to do what you want...

To achieve this goal, **we propose to adopt a very few common & clear rules (actually one).** 

> As soon as you follow this rule, you can participate to the constellations of Xamplez instances. 

### Xamplez.io's Rule

##### Literary version
> One Gist to rule them all, One Gist to find them,
>
> One Gist to bring them all and in the whatever-ness bind them

##### casual version
 * Xamplez provides the **Lord of the Gist** ie the gist that rules them all!
 * Just fork this _Lord of the Gist_ for your new instance of Xamplez: we'll call it the Root Gist.
 * All code snippets concerning your topic must fork your Root Gist.
 * From your Root Gist, using the great power of Github, you can then find all code snippets for your topic.
 * That's all folks...

### What's Xamplez.io out-of-the-box ?

 * the famous _Lord of the Gist_ which is the mandatory starting point containing 3 files by default:
    * `code` that can be renamed with your extension: for ex, `code.scala`
    * `README.md` containing xamplez guide explaining how to write code snippets
    * `_License.txt` containing a potential license for your code
    * `meta` containing metadata used by xamplez.io backend
 
 * the less known _Lord of the Conf Gist_ which is used to store all custom configurations for your Xamplez instance: 
     * custom Play configuration
     * custom CSS
     * custom HTML templates
     * local messages
     * etc...
 
 * a default Opensource server implementation capable of building a code search engine from one single entry point, your Root Gist. It's based on:
    * Play2.1/Scala + ElasticSearch for backend
    * AngularJS for frontend

 * a domain xamplez.io : we would like to gather all Xamplez instances under this domain `xyz.xamplez.io`. It's not mandatory but we think it would be cool ;)
 
> Xamplez Github project is licensed under classic Apache2 which provides the freedom we expect and will maintain the link between core projet and its forks.

### How to create a new instance of Xamplez.io

 1. Fork this github project
 2. Fork the **Lord of the Gist** to create your Root Gist
 3. Fork the **Lord of the Gist Conf** to create your Conf Gist & put Root Gist ID in it
 4. Put Conf Gist ID in conf/application.conf
 5. Fork Root Gist and create a few snippets :
     * Put a short & relevant description of your code snippet: all words preceded by `#` will be indexed as tags
     * Put your code snippet in code file and you can create more files too
    

### The team behind Xamplez.io

If you want to contact us : contact@xamplez.io

* Paul Dijou, frontend/AngularJs kungfu-guru 
* Jacques Bachellerie, backend ninja
* Gaetan Renaudeau, initiator & frontend mad master
* Pascal Voitot, initiator & backend terrible bogeyman



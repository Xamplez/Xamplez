## Welcome to Xamplez.io

> Xamplez.io, building OpenSource Knowledge Cluster of Code Snippets

### Xamplez.io's idea?

Xamplez is based on a very simple idea: 
> I want to share code snippets (and nothing else) about a given topic with a simple description 
> & be able to search them based on tags, ratings etc...

Our primary goal is to **help people build their own stores of code snippets focusing on a specific topic**. 
The topic can go from very generic subjects such as programming language or frameworks to more specific ones such as 
very precise technical points. It's up to you to do what you want...

To achieve this goal, **we propose to adopt a very few common rules (actually one).** As soon as you follow this rule, 
you are participating to the constellations of Xamplez instances _(constellation begins with number >= 2 ;))_. 

### Xamplez.io's Rule

##### Literary version
> One Gist to rule them all, One Gist to find them,
>
> One Gist to bring them all and in the happiness bind them

##### Common version
 * Xamplez provides the **Lord of the Gist** ie the gist that rules them all!
 * Just fork this _Lord of the Gist_ for your new instance of Xamplez: we call it the **Root Gist**.
 * **All code snippets concerning your topic must fork your Root Gist**.
 * From your Root Gist, using the great power of Github, you can then find all code snippets for your topic.
 * That's all folks...

### What's Xamplez.io out-of-the-box ?

Xamplez.io is completely opensource, nothing hidden because we want people to contribute to it, to improve it and
to fork it for their own needs. We would just like to try to keep a link from the core project to all its 
forks/instances to build a pure opensource knownledge cluster based on Gist.

 * the famous **Lord of the Gist** which is the mandatory starting point containing 3 files by default:
    * `code` that can be renamed with your extension: for ex, `code.scala`
    * `README.md` containing xamplez guide explaining how to write code snippets
    * `_License.txt` containing a potential license for your code
    * `meta` containing metadata used by xamplez.io backend
 
 * the less known **Lord of the Conf Gist** which is used to store all custom configurations for your Xamplez instance: 
     * custom Play configuration
     * custom CSS
     * custom HTML templates
     * local messages
     * etc...
 
 * a **default Opensource server implementation** capable of building a code search engine from one single entry point, your Root Gist. It's based on:
    * Play2.1/Scala + ElasticSearch for backend
    * AngularJS for frontend

 * a **domain xamplez.io** : we would like to gather all Xamplez instances under this domain `xyz.xamplez.io`. It's not mandatory but we think it would be cool ;)
 
> Xamplez Core Server is licensed under classic Apache2 which provides the freedom we expect and will maintain the link between core projet and its forks.

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



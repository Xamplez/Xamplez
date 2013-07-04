## Welcome to Xamplez.io

> Help us build an OpenSource Knowledge Cluster of Code Snippets

### Xamplez.io's idea?

Xamplez is based on a very simple idea: 
> I want to share code snippets (and nothing else) about a given topic with a simple description 
> & be able to search them based on tags, ratings etc...

Our primary goal is to **help people build their own stores of code snippets focusing on a specific topic**. 
The topic can go from very generic subjects such as programming language or frameworks to more specific ones such as 
very precise technical points. It's up to you to do what you want...

To achieve this goal, **we propose to adopt a very few common rules (actually one for now).** 
As soon as you follow this rule, you are implicitly participating to the constellations of Xamplez 
instances _(constellation begins with number > 1;))_. 

<br/>
### Xamplez.io's Rule

##### Literary version
> One Gist to rule them all, One Gist to find them,
>
> One Gist to bring them all and in the happiness bind them

##### Common version
 * All Xamplez is based on one single Gist called the **Lord of the Gists** that rules them all!
 * To start a new instance of Xamplez, just fork the _Lord of the Gists_ to obtain your **Root Gist**.
 * **All code snippets concerning your topic must fork your Root Gist**.

As you can imagine, using the great power of Github API, from your Root Gist, you can then find all code snippets 
for your topic.

This is the only mandatory rule for now! Now too constraining, isn't it?

<br/>
### What's Xamplez.io out-of-the-box ?

Taking this rule into account, we have built current github project to help you kickstart your own instance of
Xamplez.io for your own topic. 

First of all, we made Xamplez.io completely opensource, nothing hidden. We want people to contribute to it, 
to improve it & to fork it for their own needs and things we haven't imagined yet. 

We'd just like to keep a link between all forks/instances (following the forks of _Lord of the Gists_) because
we aim at building an opensource knownledge cluster based on Gist.

So, for now, Xamplez.io provides out-of-the-box:

#### The mandatory **Lord of the Gists**, 

It's the father of all Root gists. It provides a default format based on 4 files:

 * `code` that can be renamed with your extension: for ex, `code.scala`
 * `README.md` containing xamplez guide explaining how to write code snippets
 * `_License.txt` containing a potential license for your code
 * `meta` containing metadata used by xamplez.io backend
 
#### The useful **Master Conf Gist** 

It's used to store all custom configurations for Xamplez instance and specially the link to the Root Gist of your intance. 

It works on the same principles as the _Lord of the Gists_: to build your own configuration, you fork it and store in one or more files:

 * custom Play configuration
 * custom CSS
 * custom HTML templates
 * local messages
 * etc...
 
#### A **default Opensource server implementation** 

This is an autonomous server capable of building a code snippet search engine starting only with the ID of your Root Gist. 
It's based on:
 
 * Play2.1/Scala + ElasticSearch for backend
 * AngularJS for frontend

Please note that we don't impose anything about the backend and you can implement it as you want. But naturally, 
our default implementation provides the default mechanisms and our vision. So, we'd like people to
improve this default implementation and contribute to the github project so that everyone can take advantage of 
the same well built backend and build an instance very quickly.

> We will progressively document our backend implementation and specially the use of the configuration Gist which
is quite cool to customize your instance very easily.

#### A domain `xamplez.io`

We would like to gather all Xamplez instances under this domain `xyz.xamplez.io`. It's not mandatory but we think it would be cool ;)
 
> Xamplez Core Server is licensed under classic Apache2 which provides the freedom we expect and will maintain the link between core projet and its forks.

<br/>
### How to create a new instance of Xamplez.io

 1. Fork this github project
 2. Fork the **Lord of the Gists** to create your Root Gist
 3. Fork the **Master Conf Gist** to create your Conf Gist & put Root Gist ID in it
 4. Put Conf Gist ID in conf/application.conf
 5. Fork Root Gist and create a few snippets :

   * Put a short & relevant description of your code snippet: all words preceded by `#` will be indexed as tags
   * Put your code snippet in code file and you can create more files too
    
<br/>
### The team behind Xamplez.io

If you want to contact us : contact@xamplez.io

(in alphabetic order)

* Jacques Bachellerie, backend ninja
* Paul Dijou, frontend/AngularJs kung(fu-)guru 
* Gaetan Renaudeau, initiator & frontend ma(d)ster
* Pascal Voitot, initiator & backend terrible bogeyman



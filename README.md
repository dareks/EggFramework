Egg Framework
=============

Web application framework with following features

 * Super fast and scalable (written in Java for Java developers)
 * Allows to develop quickly (design and some solutions influenced by Ruby on Rails) - CoC, DRY, ultra fast prototyping
 * MVC pattern applied
 * Simple, yet powerful view layer leveraging Groovy programming language
 * Ready for creating REST and Flash remoting (AMF) actions
 * Create asynchronous actions for functionalities like chats, news live feeds or web browser games
 * Use your normal tools, libraries and frameworks which you like - Maven, Guava, slf4j etc.

Quick start
-----------

 * __Download IDE__ - I recommend Eclipse IDE for Java EE Developers - http://eclipse.org
 * __Install Maven plugin__ if needed (Netbeans and Intellij IDEA have Maven support out of the box) - for Eclipse use the update site of M2E plugin: http://download.eclipse.org/technology/m2e/releases
 * __Download the source code__ of Egg Framework and extract somewhere (use Downloads link in top right corner of this page or clone the Git repository)
 * __Import the project__ into your IDE - in Eclipse M2E use _File/Import/Existing Maven Projects_ and select the extracted directory
 * __run__ the class _framework.Server_ located in _src/main/java_ - the embedded server with __sample code__ will be started 
 * __go to__ http://localhost:8080 in your browser
 * check the source code in [src/main/java/controllers/SampleController.java](EggFramework/blob/master/src/main/java/controllers/SampleController.java) class, try to change it and see what will happen. If you don't have [JRebel](http://www.zeroturnaround.com/jrebel/) you need to restart the embedded server every time you change the Java code (just stop the process and run it again). For template changes restarting is not required.
 
History
-------

First version of Egg Framework was developed by Jacek Olszak in 2008. At that time the framework was just a presentation layer framework like JSP, Velocity or Freemarker. Instead of writing templates
developer was creating a Java classes which generates the HTML markup. 
After one year of use the author realized that it is not the proper way - mainly because of the performance problems (slow response time), high memory consumption (whole html tree held in memory) and heavy
use of session (the generated pages were saved into session). So he created a second version of 
Egg Framework with template engine based on Groovy and decided also to write the simple yet powerful controller layer. He was really impressed by other frameworks available like Ruby on Rails or Grails but 
they were too slow to meet his needs (mostly because of the programming languages used by them). 
He wanted to have a very simple and high performant framework for Java language (which is fast enough) which can be used for rapid web application development. Now the new Egg Framework is almost ready and can be used for standard websites, 
AJAX applications and even games development (which in fact is the most important for the author :).

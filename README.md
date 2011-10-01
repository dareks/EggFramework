Egg Framework
=============

Full stack web application framework with following features

 * Super fast and scalable (written in Java for Java developers)
 * Allows to develop quickly (design influenced by Ruby on Rails) - CoC, DRY, ultra fast prototyping
 * MVC pattern applied
 * Simple, yet powerful view layer leveraging Groovy programming language
 * Integration with MongoDB and Morphia - rapid development of database backed applications with scalablity in mind
 
Quick start
-----------

 * __Download IDE__ - I recommend Eclipse IDE for Java EE Developers - http://eclipse.org
 * __Install Maven plugin__ if needed (Netbeans and Intellij IDEA have Maven support out of the box) - for Eclipse use the update site of M2E plugin: http://download.eclipse.org/technology/m2e/releases
 * __Download the source code__ of Egg Framework and extract somewhere
 * __Import the project__ into your IDE - in Eclipse M2E use _File/Import/Existing Maven Projects_ and select the extracted directory
 * __run__ the class _framework.Server_ located in _src/main/java_ - the embedded server with sample code will be started 
 * go to http://localhost:8080
 * check the source code in [src/main/java/controllers/SampleController.java](EggFramework/blob/master/src/main/java/controllers/SampleController.java) class, try to change it and see what will happen. If you don't have [JRebel](http://www.zeroturnaround.com/jrebel/) you need to restart the embedded server every time you change the Java code (just stop the process and run it again).
 

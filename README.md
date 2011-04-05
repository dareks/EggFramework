# Egg Framework

Full stack web application framework with following features

* Super fast and scalable (written in Java for Java developers)
* Allows to develop rapidly (design influenced by Ruby on Rails) - CoC, DRY, ultra fast prototyping
* MVC pattern applied
* Simple, yet powerful view layer leveraging Groovy programming language

## Sample code

src/main/java/controllers/PostsController.java:

```java
 public class PostsController {
    // executed for url: /game/postDetails?id=12
    // will render /game/postDetails.html
    public void postDetails() {
      int id = paramAsInt("id");
      Post post = ....
      attr("post", post);
    }
    
    // executed by Flash player using AMF HTTP
    // will returnJava String as an ActionScript String
    public String flashAmfTest() {
      return "Hello from server :)";
    }
 }
```
  
src/main/resources/posts/postDetails.html:

```html
  <html>
  <body>
  	<%= post %>
  </body>
  </html>
```
# spring-vaadin-groovy
Utilities and demo project for spring boot and vaadin web development with groovy and java.
Utilities support 
* a simpler handling of associations between domain classes
* a vaadin builder based on groovy builder support
* a simple state machine for gui state control

The demo app integrates spring boot, vaadin, vaadin security and demonstrates the use of the utilities.
A developing with [spring-vaadin-groovy tutorial](http://geobe.de) is under construction, at the moment in German.
## Association support
While associations are always bidirectional in relational databases, we have to implement and synchronize both sides 
in java or groovy code. This is automatically handled by the association utilities, givig a unified and less error prone
interface to associations.
## A groovy vaadin builder
Vaadin user interface classes can become quite large and confusing. With support of a groovy VaadinBuilder, 
this [can be much simpler](http://www.georgbeier.de/tutorials-java-und-mehr/java8-spring-groovy-vaadin/groovy-vaadin-builder/).
## Controlling UI state with a lightweight DialogStateMachine
UI classes can become simpler when controlling states with a state machine
## Integrating Vaadin and Spring security
On the [Vaadin4Spring](https://github.com/peholmst/vaadin4spring) website of Petter Holmström, all the necessary classes
for the security integration are available. But it took me quite some time to get it all working, 
so I [wrote a bit](http://www.georgbeier.de/tutorials-java-und-mehr/java8-spring-groovy-vaadin/spring-vaadin-security-integration/) 
about it (at the moment in German).


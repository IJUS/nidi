NiDI
=================
### Non-invasive Dependency Injection ###

NiDI (needy, get it?) provides a simple solution for dependency injection for Groovy applications. NiDI provides an application Context that takes care of instantiating all of the dependencies for you. Configuration is done entirely in code and is very easy to setup and test, while still maintaining a very high level of flexibility. There is no xml and error messages try to be a helpful as possible.
	
## Hello World ##

In this example, we'll setup a simple context in which the interface `SearchService` is bound to the implementation `BasicFileSystemSearchService`.
	
	Context ctx = Configuration.configureNew {
		bind(SearchService).to(BasicFileSystemSearchService)
		//tells the context which implementation to use for SearchService
	}
	
	//whenever we need a searchService...
	def searchService = ctx.getInstance(SearchService)
	assert searchService instanceof BasicFileSystemSearchService
	
A Context takes care of instantiating the concrete implementations of all the classed that are bound in it. Typically, an application should have a single main context, which NiDI provides a static place for in the `ContextHolder` class. A simple way to bootstrap your application is to provide an implementation of `net.ijus.nidi.ContextConfig` and tell the Configuration class to set things up from that. 

*Example ContextConfig:*

	//ExampleConfigScript.groovy
	package com.example.config
    
    import com.example.impl.*
    import com.example.interfaces.*
    import net.ijus.nidi.*   
    
    public class ExampleConfigScript implements ContextConfig {
    
    	@Override
    	void configure(Context ctx) {
    
    		ctx.bind(LoggingService).to(LoggingServiceImpl)
    	}
    }
    
    //when the application bootstraps, you have some options...
    ...
    public static void main(String[] args){
		
        Class contextConfigClass = determineContext()
        Configuration.setMainContextFromClass(contextConfigClass)
        //now run application normally
        
        Context ctx = ContextHolder.getContext()
        assert ctx.getInstance(LoggingService) instanceof LoggingServiceImpl
    }
    
Or, if you really want a simple way to externalize which configuration to use, you can just set the system property, "nidi.context.class" to the fully qualified class name of a class that implements ContextConfig and let nidi figure it out.


## Default Bindings ##

A common use case is to have a set of default bindings that are sometimes overridden for specific contexts. Contexts are can inherit from one another, so this is super easy:

	//in ExampleConfigScript.groovy
	...
	void configure(Context ctx){
		
		ctx.inheritFrom(MyDefaultConfigClass)
		ctx.bind(SearchService).to(DifferentSearchService)
		...
	}
	
## Setting basic properties ##

It may happen that an instance will need some basic property set before it's available to use. This might be something like just setting the URL for a particular service. These situations should be kept to a minimum because it isn't obvious when a property is being injected, but there are certainly times when this is the best solution. If a simple string is all your changing, there's no need to create a whole new implementation. Just use the `Binding.setupInstance(Closure)` method.

	//configuring context...
	ctx.bind(ThisInterface).to(ThatImplementation).setupInstance {ThatImplementation instance->
		//this closure will be called every time a new instance of ThatImplementation is created
		instance.setSomeProperty("new value")
	}

## Complex Hierarchies ##

These are less than ideal, but every code base of significant size has them, and NiDI makes it very easy to setup. In the following example, we have an interface CreditCardProcessor and we want to use the implementation `com.example.impl.ComplexCCProcessor`, which itself depends on two other services. The `ComplexCCProcessor` depends on a `FraudDetectionService` and a `LoggingService`. Here, NiDI took a cue from Google's Guice and forces all dependencies to simply be declared in the Constructor. Here's a look at the ComplexCCProcessor:

	//ComplexCCProcessor.groovy
	...
	FraudDetectionService fraudDetectionService
	LoggingService loggingService
	
	ComplexCCProcessor(FraudDetectionService fds, LoggingService logServ){
		this.fraudDetectionService = fds
		this.loggingService = logServ
	}
	
What if your implementation of `LoggingService` itself has additional dependencies? What if I want two different classes that depend on the same service to use the same instance? These scenarios could easily become ridiculous to try to manage all the dependencies, so NiDI will take care of all the dependencies automatically, even if your tree is huge. Just make sure that all of the required classes have bindings in the context.

*Scoping*

Sometimes it's important that two classes use the same instance of a particular class. Scoping configuration in NiDI is exceedingly simple, but also allows for fine grained control over how instances get scoped, cached, and reused. When you bind a class to an implementation, you can specify a scope. There are four scopes:

net.ijus.nidi.Scope:
 
 - SINGLETON - If you use this scope, there will only ever be a single instance of the implementation, no matter how many contexts it's used in. 
 - CONTEXT\_GLOBAL - This scope means that each context will have it's own instance of the implementation class. This instance will be used for everything in that context.
 - ONE\_PER\_BINDING - Each time you `bind(Interface).to(Implementation)` you will get a single instance of the Implementation class.
 - ALWAYS\_CREATE\_NEW - This is the default scope. Every time you call `context.getInstance()`  new instances of classes scoped as `ALWAYS\_CREATE\_NEW` will be created.
 
 Scopes can be applied as a detault for an entire context, and can also be set individually for specific implementation classes. 
        
    //configuring a context with scopes
    ..
    ctx.defaultScope = Scope.ALWAYS\_CREATE\_NEW //All bindings in this context will inherit this scope unless you say differently
    
    bind(LoggingService).withScope(Scope.SINGLETON).to(LoggingServiceImpl)
    //ALL calls to ctx.getInstance(LoggingService) will now return the same instance of LoggingServiceImpl
    //This is true even if you make the call from another context!
    

# Limitations / Known Issues #

- Currently, instantiation will fail if the implementation class has more than one public constructor. This limitation may or may not be lifted by adding a constructor annotation.
- 

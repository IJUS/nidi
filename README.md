NiDI
=================
### Non-invasive Dependency Injection ###

NiDI (needy, get it?) is a simple dependency injection library for Java applications. NiDI provides an application Context that takes care of instantiating all Class dependencies for you. Configuration is done entirely in code and is very easy to setup and test, while still maintaining a very high level of flexibility. There is no xml and error messages try to be as helpful as possible.

# The Core Principals

1. **Separate Dependency Resolution from Bussiness Logic**. This is ultimately what we're after, and what NiDI enables.
2. **Write code, not XML**. There's no reason to write configuration files in XML when you could use the full power of the Java (or Groovy) language instead. Configurations can inherit from one another, allowing a great deal of code reuse.
3. **Configuration should be testable**. Configuration errors are one of the most common causes of problems there is. Since NiDI configuration is done entirely in code, there's no excuse for not testing it. In fact, we try to make testing as easy as possible.
4. **Simple and non-invasive**. Other dependency injection libraries can be huge, bringing in many dependencies of their own. NiDI strives to be lean. Its only dependency is the Logging facade, SLF4J. While other libraries use things like dynamic Class generation to do their work, NiDI stays with standard Java.


# Hello World #

In this example, we'll setup a simple context in which the interface `SearchService` is bound to the implementation `BasicFileSystemSearchService`.
	
	Context ctx = Configuration.configureNew {
		bind(SearchService).to(BasicFileSystemSearchService)
		//tells the context which implementation to use for SearchService
	}
	
	//whenever we need a searchService...
	def searchService = ctx.getInstance(SearchService)
	assert searchService instanceof BasicFileSystemSearchService
	
A Context takes care of instantiating the concrete implementations of all the classed that are bound in it. Even complex nested dependencies are resolved and instantiated automatically. This has huge benefits for almost any application. It allows dependency resolution to be separated from business logic, or even externalized, and makes it easily testable. 


## A (slightly) more involved example

Say we have two classes, Foo and Bar:

	class Foo implements MyInterface {
		Bar bar
		
		Foo(Bar bar){
			this.bar = bar
		}
		...
	}
	
	class Bar {
		String barProperty
		
		Bar(String barProperty){
			this.barProperty = barProperty
		}
	}
	
	//in whatever is using MyInterface...
	String s = resolveBarProperty()
	Bar bar = new Bar(s)
	MyInterface foo = new Foo(bar)
	foo.finallyDoSomethingWithFoo()
	
In this example, Foo depends on Bar, which needs a string in order to work properly. Normally, the developer would have to resolve which String to use for Bar, then create a new Bar with the string, then create a new Foo with the Bar. This can result in a lot of code that has little to do with the task at hand. The NiDI way moves all of that into the configuration class and provides a simple, declarative API for it:
	
	//MyContextConfig.java
	...
	bind(MyInterface).to(Foo) //tells nidi that whenever I ask for a `MyInterface`, you should provide me with a `Foo`
	BindingBuilder barBuilder = register(Bar) //makes nidi aware of Bar and able to instantiate it as needed
	barBuilder.bindConstructorParam(String).toObject('myString') 
	//this just takes a closure that returns the correct value
	
	//In whatever is using a Foo...
	def foo = context.getInstance(MyInterface)
	
So there's a couple of differences here. First off, notice that in whatever method is actually using MyInterface, there's no logic for determining which implementation to use. Secondly, there's no code for manually creating a Bar. Instead, the Context takes care of that. The Context can, replace the word `new` in your code, and in most cases eliminates the need for Factories.

## Configuration

Typically, an application should have a single main context, which NiDI provides a static place for in the `ContextHolder` class. A simple way to bootstrap your application is to provide an implementation of `net.ijus.nidi.ContextConfig` and tell the Configuration class to set things up from that. 

*Example ContextConfig:*

	//ExampleConfigClass.java
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
		
        Configuration.setMainContextFromClass(MyConfigClass.class)
        //now run application normally
        
        Context ctx = ContextHolder.getContext()
        assert ctx.getInstance(LoggingService) instanceof LoggingServiceImpl
    }
    
Or, if you really want a simple way to externalize which configuration to use, you can just set the system property, "nidi.context.class" to the fully qualified class name of a class that implements ContextConfig and let nidi figure it out.

## Validation ##

NiDI aims to fail fast when there is a problem with the configuration. Contexts and Bindings get validated when the `ContextBuilder.build()` method is called. Any invalid configurations will result in an InvalidConfigurationException being thrown. Validation ensures that every Implementation Class in the Context has all of it's dependencies met. This means that every Constructor parameter must have a corresponding non-null Binding. This requirement is relaxed for constructor parameters bearing the `@Optional` annotation.   


## Default Bindings ##

A common use case is to have a set of default bindings that are sometimes overridden for specific contexts. Contexts are can inherit from one another, so this is super easy:

	//in ExampleConfigClass.java
	...
	void configure(Context ctx){
		
		ctx.inheritFrom(MyDefaultConfigClass)
		ctx.bind(SearchService).to(DifferentSearchService)
		...
	}
	
## Setting basic properties ##

It may happen that an instance will need some basic property set before it's available to use. This might be something like just setting the URL for a particular service. These situations should be kept to a minimum because it isn't obvious when a property is being injected, but there are certainly times when this is the best solution. If a simple string is all your changing, there's no need to create a whole new implementation. Just use the `Binding.setupInstance(InstanceSetupFunction)` method.

	//configuring context...
	ctx.bind(ThisInterface).to(ThatImplementation).setupInstance( new InstanceSetupFunction<ThatImplementation>() {
	    @Override
	    void setup(ThatImplementation instance){
	        instance.setSomeProperty("new value")
	    }
	})


## Complex Hierarchies ##

These are less than ideal, but every code base of significant size has them, and NiDI makes it very easy to setup. In the following example, we have an interface CreditCardProcessor and we want to use the implementation `com.example.impl.ComplexCCProcessor`, which itself depends on two other services. The `ComplexCCProcessor` depends on a `FraudDetectionService` and a `LoggingService`. Here, NiDI took a cue from Google's Guice and forces all dependencies to simply be declared in the Constructor. Here's a look at the ComplexCCProcessor:

	//ComplexCCProcessor.java
	...
	FraudDetectionService fraudDetectionService
	LoggingService loggingService
	
	ComplexCCProcessor(FraudDetectionService fds, LoggingService logServ){
		this.fraudDetectionService = fds
		this.loggingService = logServ
	}
	
What if your implementation of `LoggingService` itself has additional dependencies? What if I want two different classes that depend on the same service to use the same instance? These scenarios can easily add a ton of  Just make sure that all of the required classes have bindings in the context.

*Scoping*

Sometimes it's important that two classes use the same instance of a particular class. Scoping configuration in NiDI is exceedingly simple, but also allows for fine grained control over how instances get scoped, cached, and reused. When you bind a class to an implementation, you can specify a scope. There are three scopes:
 
 - SINGLETON - If you use this scope, then the Binding will always return the same instance, every time.
 - ONE\_PER\_BINDING - Each Binding that requires an instance of this class (as a Constructor param) will get it's own instance that will always be the same. So if the Classes Foo and Bar both depend on a LoggingService, which is scoped as ONE\_PER\_BINDING, then both would have their own instance of LoggingService. The instance of LoggingService given to the Foo Constructor would always be the same, though.
 - ALWAYS\_CREATE\_NEW - This is the default scope. Every time you call `context.getInstance()`  new instances of classes scoped as `ALWAYS\_CREATE\_NEW` will be created.
 
 Scopes can be applied as a detault for an entire context, and can also be set individually for specific implementation classes. 
        
    //configuring a context with scopes
    ..
    ctx.defaultScope = Scope.ALWAYS\_CREATE\_NEW //All bindings in this context will inherit this scope unless you say differently
    
    bind(LoggingService).withScope(Scope.SINGLETON).to(LoggingServiceImpl)
    //ALL calls to ctx.getInstance(LoggingService) will now return the same instance of LoggingServiceImpl
    //This is true even if you make the call from another context!
    

# Advanced Usage

**Classes with multiple constructors**
If a Class has multiple constructors, you'll have to tell NiDI which one to use by annotating one of them with @Inject.

**Bound Properties**
Oftentimes, we'll have some property, a URL perhaps, that is required for several classes. NiDI addresses this use case by allowing bound properties. In the configuration for a context, you can call ContextBuilder's bindProperty() method to accomplish this.

	//given the following class that requires a url
	class UploadService {
		String url
		
		UploadService(@RequiredBinding("uploadServURL") String url){
			this.url = url
		}
		...
	}
	
	//In the context configuration:
	bindProperty("uploadServURL", "www.website.com") //you can bind to any object, or to an InstanceGenerator that can return whatever value you like
	
Now, when you ask the Context for an instance of UploadService (or anything else that depends on that url property), it will come properly configured. The Context can also store any other properties you'd like to access. And, since we're doing configuration in code instead of XML properties can be anything, not just Strings. Any object can be bound to a property in the context.

**Overrides for nested dependencies**
Let's say we have many classes that all depend on our SearchInterface. In many cases, we'll want to use the same implementation for all of those classes, but not always. If we have some situations where we want to override the Binding that's in the context, we can simply declare the proper bindings using the bindConstructorParam() method in BindingBuilder.

	bind(SearchService).to(BasicSearchService) //use this for everything, unless an override is present
	BindingBuilder bindingBuilder = bind(MyInterface).to(MyImpl)
	bindingBuilder.bindConstructorParam(SearchService).to(ComplexSearchService)
	//This same method works with bound properties, too. 

If a constructor param is annotated with @RequiredBinding('name') or @Optional('name'), you could also use: bindingBuilder.bindConstructorParam('name')

**Null Values**
Every constructor parameter for every Class in the Context must have a corresponding binding. If a constructor parameter should be null, two conditions must be satisfied. First, it must be explicitly bound to null using the BindingBuilder.toNull() method. Secondly, the constructor parameter must be annotated with `@Optional` to indicate that a NullBinding is acceptable. 
package net.ijus.nidi.builder

import net.ijus.nidi.Configuration
import net.ijus.nidi.Context
import net.ijus.nidi.ContextConfig
import net.ijus.nidi.InvalidConfigurationException
import spock.lang.*
import com.example.general.UnannotatedConstructors

class MultipleConstructorsSpec extends Specification {

	void "binding builder should choose one of multiple constructors based on the bindings available"(){
		setup:
		ContextConfig config = {ContextBuilder builder->
			BindingBuilder<UnannotatedConstructors> bb = builder.register(UnannotatedConstructors)
			bb.bindConstructorParam("requiredString").toObject("myRequiredString")

		} as ContextConfig

		when:
		Context ctx = Configuration.configureNew(config)
		UnannotatedConstructors instance = ctx.getInstance(UnannotatedConstructors)

		then:
		notThrown(InvalidConfigurationException)
		instance.one == "myRequiredString"
		instance.two == null
	}
}
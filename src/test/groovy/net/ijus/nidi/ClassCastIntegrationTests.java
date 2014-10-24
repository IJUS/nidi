package net.ijus.nidi;

import com.example.impl.ComplexCCProcessor;
import com.example.interfaces.CreditCardProcessor;
import net.ijus.nidi.bindings.Binding;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClassCastIntegrationTests {

	@BeforeClass
	public static void setupClass(){
		try {
			Configuration.setMainContextFromClass(com.example.config.ComplexConfigScript.class);
		} catch (Exception e) {
			System.out.println("Exception thrown setting up context");
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void cleanupClass(){
		ContextHolder.setContext(null);
	}

	@Test
	public void testGetInstance() throws Exception {
		/*
		The real test here is just that this class compiles without having to cast
		the return value of Context.getInstance(Class)
		 */
		CreditCardProcessor ccProc  = ContextHolder.getCtx().getInstance(CreditCardProcessor.class);
		assertTrue(ccProc instanceof ComplexCCProcessor);

		Binding<CreditCardProcessor> ccBinding = ContextHolder.getCtx().getBinding(CreditCardProcessor.class);
		CreditCardProcessor ccProc2 = ccBinding.getInstance();
		assertEquals(ccProc, ccProc2);
	}
}
/*******************************************************************************
 * TODO: explanation what the class does
 *  
 *  @author Kevin Feichtinger
 *  
 * Copyright 2023 Johannes Kepler University Linz
 * LIT Cyber-Physical Systems Lab
 * All rights reserved
 *******************************************************************************/
package at.jku.cps.travart.dopler.decision.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import at.jku.cps.travart.dopler.decision.model.impl.BooleanDecision;
import at.jku.cps.travart.dopler.decision.model.impl.EnumerationDecision;

@SuppressWarnings("rawtypes")
public class AFunctionTest {

	private AFunction af;
	private BooleanDecision b;
	private AFunction af2;
	private AFunction af3;
	private AFunction af4;
	private AFunction af5;

	@BeforeEach
	public void init() {
		b = new BooleanDecision("test");
		af = new AFunction("test", b) {

			@Override
			public boolean evaluate() {
				return b.isSelected();
			}

			@Override
			public Object execute() {
				return null;
			}
		};
		af2 = new AFunction("test", b) {

			@Override
			public boolean evaluate() {
				return b.isSelected();
			}

			@Override
			public Object execute() {
				return null;
			}
		};

		af3 = new AFunction("test2", b) {

			@Override
			public boolean evaluate() {
				return b.isSelected();
			}

			@Override
			public Object execute() {
				return null;
			}
		};
		af4 = new AFunction("test2", new EnumerationDecision("test")) {

			@Override
			public boolean evaluate() {
				return false;
			}

			@Override
			public Object execute() {
				return null;
			}
		};
		af5 = new AFunction("test", new EnumerationDecision("test")) {

			@Override
			public boolean evaluate() {
				return false;
			}

			@Override
			public Object execute() {
				return null;
			}
		};
	}

	@Test
	public void noNullNameTest() {
		assertThrows(NullPointerException.class, () -> {
			new AFunction<>(null, new LinkedList<IDecision>()) {

				@Override
				public boolean evaluate() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public IDecision<?> execute() {
					// TODO Auto-generated method stub
					return null;
				}

			};
		});
	}

	@Test
	@SuppressWarnings("unused")
	public void noNullListTest_shortConstructor() {
		List<IDecision> l = null;
		assertThrows(NullPointerException.class, () -> {
			AFunction<IDecision> a = new AFunction<>("aName", l) {

				@Override
				public boolean evaluate() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public IDecision<?> execute() {
					// TODO Auto-generated method stub
					return null;
				}

			};
		});
	}

	@Test
	@SuppressWarnings("unused")
	public void noNullListTest_longConstructor() {
		List<IDecision> l = null;
		assertThrows(NullPointerException.class, () -> {
			AFunction<IDecision> a = new AFunction<>("aName", null, null, null) {

				@Override
				public boolean evaluate() {
					// TODO Auto-generated method stub
					return false;
				}

				@Override
				public IDecision<?> execute() {
					// TODO Auto-generated method stub
					return null;
				}

			};
		});
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSetGetParameters() {
		assertTrue(af.getParameters().contains(b));
		List<IDecision> par = new LinkedList<>();
		EnumerationDecision ed = new EnumerationDecision("test");
		par.add(ed);
		par.add(b);
		af.setParameters(par);
		assertTrue(af.getParameters().contains(b) && af.getParameters().contains(ed));
	}

	@Test
	public void testHash() {
		assertEquals(af.hashCode(), af.hashCode());
		assertEquals(af.hashCode(), af2.hashCode());
		assertNotEquals(af.hashCode(), af3.hashCode());
		assertNotEquals(af.hashCode(), af4.hashCode());
		assertNotEquals(af.hashCode(), af5.hashCode());
	}

	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEquals() {
		assertFalse(af.equals(null));
		assertFalse(af.equals("aString"));
		assertTrue(af.equals(af));
		assertTrue(af.equals(af2));
		assertFalse(af.equals(af3));
		assertFalse(af.equals(af4));
		assertFalse(af.equals(af5));
	}
}

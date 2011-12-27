package oqube.patchwork.graph;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import oqube.bytes.ClassFile;
import oqube.bytes.Opcodes;
import oqube.bytes.attributes.CodeAttribute;
import oqube.bytes.instructions.Instruction;
import oqube.bytes.struct.MethodFileInfo;
import junit.framework.TestCase;

public class CallMatcherTest extends TestCase {

	private List insts;
	private CallMatcher matcher;

	protected void setUp() throws Exception {
		super.setUp();
		/* create classfile object */
		ClassFile cf = new ClassFile();
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				"oqube/patchwork/graph/ControlGraph.class");
		cf.read(new DataInputStream(is));
		MethodFileInfo mfi = (MethodFileInfo) cf.getMethodInfo("parseCode","(Ljava/util/List;I)V");
		CodeAttribute code = mfi.getCodeAttribute();
		insts = code.getAllInstructions();
		matcher = new CallMatcher();
	}

	public void test01MatchVirtualCall() {
		/* find an invoke instruction */
		Instruction in = null;
		for (Iterator i = insts.iterator(); i.hasNext();)
			if ((in = (Instruction) i.next()).opcode() == Opcodes.opc_invokevirtual)
				break;
		
		assertTrue("Matcher should match "+in,matcher.match(in));
	}

	public void test02MatchIfaceCall() {
		/* find an invoke instruction */
		Instruction in = null;
		for (Iterator i = insts.iterator(); i.hasNext();)
			if ((in = (Instruction) i.next()).opcode() == Opcodes.opc_invokeinterface)
				break;
		assertTrue("Matcher should match "+in,matcher.match(in));
	}

	public void test03MatchSpecialCall() {
		/* find an invoke instruction */
		Instruction in = null;
		for (Iterator i = insts.iterator(); i.hasNext();)
			if ((in = (Instruction) i.next()).opcode() == Opcodes.opc_invokespecial)
				break;
		matcher.setClassPattern(Pattern.compile("java/util/.*"));
		matcher.setMethodPattern(Pattern.compile(".*init.*"));
		assertTrue("Matcher should match "+in,matcher.match(in));
	}


}

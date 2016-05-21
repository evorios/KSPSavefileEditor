package at.woelfel.philip.kspsavefileeditor.backend;

import org.testng.annotations.Test;

import static at.woelfel.philip.kspsavefileeditor.backend.TreeAsserts.then;

public class ParserTest {
	@Test
	public void testNothingInEmptyContent() throws Exception {
		then(doParse("", true)).isNull();
		then(doParse("", false)).isNull();
	}

	@Test
	public void testEmptyNode() throws Exception {
		final Node node = doParse("A\n{\n}");
		then(node).isNotNull().hasName("A").hasNoSubnodes().hasNoEntries();
	}

	@Test
	public void testSimpleTree() throws Exception {
		final Node node = doParse("A\n" +
				"{\n" +
				"\tname = value\n" +
				"\tSUB\n" +
				"\t{\n" +
				"\t\tsubname = subvalue\n" +
				"\t}\n" +
				"}");
		then(node).isNotNull().hasName("A");
		then(node.getSubNodeCount()).isEqualTo(1);
		then(node.getEntryCount()).isEqualTo(1);
		then(node.getEntry(0)).isNotNull().hasKey("name").hasValue("value");
		then(node.getSubNode(0)).isNotNull().hasName("SUB");
		then(node.getSubNode(0).getEntry(0)).isNotNull().hasKey("subname").hasValue("subvalue");
	}

	private Node doParse(String content) throws Exception {
		return doParse(content, true);
	}

	private Node doParse(String content, boolean withRootNode) throws Exception {
		final Parser parser = new Parser(content);
		return parser.parse(withRootNode);
	}

}

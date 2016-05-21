package at.woelfel.philip.kspsavefileeditor.backend;

import org.assertj.core.api.BDDAssertions;

@SuppressWarnings("WeakerAccess")
public class TreeAsserts extends BDDAssertions {
	public static NodeAssert then(Node actual) {
		return new NodeAssert(actual);
	}

	public static EntryAssert then(Entry actual) {
		return new EntryAssert(actual);
	}
}

package at.woelfel.philip.kspsavefileeditor.backend;

import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.BDDAssertions.then;

public class EntryAssert extends AbstractAssert<EntryAssert, Entry> {
	protected EntryAssert(Entry actual) {
		super(actual, EntryAssert.class);
	}

	public EntryAssert hasKey(String name) {
		then(actual.getKey()).isEqualTo(name);
		return myself;
	}

	public EntryAssert hasValue(String name) {
		then(actual.getValue()).isEqualTo(name);
		return myself;
	}
}

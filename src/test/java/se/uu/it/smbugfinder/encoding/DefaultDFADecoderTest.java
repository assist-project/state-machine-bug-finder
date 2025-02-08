package se.uu.it.smbugfinder.encoding;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.ListAlphabet;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.automaton.fsa.impl.FastDFA;
import net.automatalib.automaton.fsa.impl.FastDFAState;
import net.automatalib.util.automaton.Automata;
import net.automatalib.word.Word;
import se.uu.it.smbugfinder.dfa.DFAAdapter;
import se.uu.it.smbugfinder.dfa.InputSymbol;
import se.uu.it.smbugfinder.dfa.OutputSymbol;
import se.uu.it.smbugfinder.dfa.Symbol;
import se.uu.it.smbugfinder.utils.TestUtils;

/*
 * TODO It would be better if our inputs for specs/expected automata were .dot files, as it would allow us to reduce the code/create better tests.
 */
public class DefaultDFADecoderTest {
//
//	@Test
//	public void testUnfoldOtherNonCapturingFilter() {
//		SymbolToken aTok = new SymbolToken(true, "a");
//		SymbolToken bTok = new SymbolToken(true, "b");
//
//		SpecificationLabel aLabel = new SpecificationLabel(aTok);
//		SpecificationLabel bLabel = new SpecificationLabel(bTok);
//		SpecificationLabel other = new SpecificationLabel(new OtherToken());
//		SpecificationLabel aOrB = new SpecificationLabel( new RegexToken(MessageType.INPUT_PREFIX + "(?:a|b)") );
//
//		List<SpecificationLabel> specLabels = Arrays.asList(aLabel, bLabel, aOrB, other);
//
//		FastDFA<SpecificationLabel> spec = new FastDFA<>(new SimpleAlphabet<>(specLabels));
//
//		FastDFAState s0 = spec.addInitialState(true);
//		FastDFAState s1 = spec.addState(true);
//		FastDFAState s2 = spec.addState(true);
//		FastDFAState sink = spec.addState(false);
//
//		spec.addTransition(s0, aLabel, s1);
//		spec.addTransition(s0, other, sink);
//		spec.addTransition(s1, bLabel, s2);
//		spec.addTransition(s1, other, sink);
//		spec.addTransition(s2, other, s2);
//		spec.addTransition(s2, aOrB, sink);
//		spec.addTransition(sink, other, sink);
//
//		MessageLabel a = new MessageLabel(true, "a");
//		MessageLabel b = new MessageLabel(true, "b");
//		MessageLabel c = new MessageLabel(true, "c");
//		List<MessageLabel> messageLabels = Arrays.asList(a, b, c);
//
//		FastDFA<MessageLabel> unfolded = DtlsLanguageAdapterBuilder.unfold(spec, messageLabels);
//
//		Assert.assertArrayEquals(new Object [] {a, b, c}, unfolded.getInputAlphabet().toArray());
//		Assert.assertFalse(unfolded.accepts(Arrays.asList(b)));
//		Assert.assertFalse(unfolded.accepts(Arrays.asList(a, a)));
//		Assert.assertTrue(unfolded.accepts(Arrays.asList(a, b)));
//		Assert.assertTrue(unfolded.accepts(Arrays.asList(a, b, c)));
//		Assert.assertFalse(unfolded.accepts(Arrays.asList(a, b, a)));
//	}
//
//	@Test
//	public void testUnfoldOtherCapturingFilter() {
//		MessageLabel chRsa = new MessageLabel(true, "chrsa");
//		MessageLabel chPsk = new MessageLabel(true, "chpsk");
//		FilterLabel chStoreKex = new FilterLabel(MessageLabel.INPUT_PREFIX + "ch(.*)");
//		FilterLabel chSameKex = new FilterLabel(MessageLabel.INPUT_PREFIX + "ch\\1");
//		FilterLabel chDifferentKex = new FilterLabel(MessageLabel.INPUT_PREFIX + "ch(?!\\1).*");
//		FilterLabel chAnyKex = new FilterLabel(MessageLabel.INPUT_PREFIX + "ch.*");
//		OtherLabel other = OtherLabel.other();
//
//		List<Label> specLabels = Arrays.asList(chRsa, chPsk,chSameKex, chStoreKex, chDifferentKex, chAnyKex, other);
//		List<MessageLabel> messageLabels = Arrays.asList(chRsa, chPsk);
//
//		FastDFA<Label> spec = new FastDFA<>(new SimpleAlphabet<>(specLabels));
//
//		FastDFAState s0 = spec.addInitialState(false);
//		FastDFAState s1 = spec.addState(false);
//		FastDFAState s2 = spec.addState(true);
//		FastDFAState s3 = spec.addState(false);
//
//		spec.addTransition(s0, chStoreKex, s1);
//		spec.addTransition(s1, chSameKex, s2);
//		spec.addTransition(s1, chDifferentKex, s3);
//		spec.addTransition(s2, other, s2);
//		spec.addTransition(s3, other, s3);
//
//		FastDFA<MessageLabel> expected =  new FastDFA<>(new SimpleAlphabet<>(messageLabels));
//		s0 = expected.addInitialState(false);
//		s1 = expected.addState(false);
//		s2 = expected.addState(false);
//		s3 = expected.addState(true);
//		FastDFAState s4 = expected.addState(false);
//		expected.addTransition(s0, chRsa, s1);
//		expected.addTransition(s0, chPsk, s2);
//		expected.addTransition(s1, chRsa, s3);
//		expected.addTransition(s1, chPsk, s4);
//		expected.addTransition(s2, chRsa, s4);
//		expected.addTransition(s2, chPsk, s3);
//		expected.addTransition(s3, chRsa, s3);
//		expected.addTransition(s3, chPsk, s3);
//		expected.addTransition(s4, chRsa, s4);
//		expected.addTransition(s4, chPsk, s4);
//
//
//		FastDFA<MessageLabel> unfolded = DtlsLanguageAdapterBuilder.unfold(spec, messageLabels);
//		assertEquivalent(expected, unfolded, messageLabels, spec, specLabels);
//	}
//
//	@Test
//	public void testUnfoldActual() {
//		SymbolToken chRsaTok = new SymbolToken(true, "chrsa");
//		SymbolToken hvrTok = new SymbolToken(false, "hvr");
//		SymbolToken shTok = new SymbolToken(false, "sh");
//
//		SpecificationLabel chRsaLabel = new SpecificationLabel(chRsaTok);
//		SpecificationLabel hvrLabel = new SpecificationLabel(hvrTok);
//		SpecificationLabel shLabel = new SpecificationLabel(shTok);
//		SpecificationLabel chAny = new SpecificationLabel(new RegexToken(MessageType.INPUT_PREFIX + "ch.*"));
//		SpecificationLabel anyOutput = new SpecificationLabel(new RegexToken(MessageType.OUTPUT_PREFIX + ".*"));
//
//		SpecificationLabel other = new SpecificationLabel(new OtherToken());
//
//
//
//		List<SpecificationLabel> specLabels = Arrays.asList(chRsaLabel, hvrLabel, chAny, anyOutput, shLabel, other);
//
//		FastDFA<SpecificationLabel> spec = new FastDFA<>(new SimpleAlphabet<>(specLabels));
//
//		FastDFAState s0 = spec.addInitialState(false);
//		FastDFAState s1 = spec.addState(false);
//		FastDFAState s2 = spec.addState(false);
//		FastDFAState s3 = spec.addState(false);
//		FastDFAState s4 = spec.addState(true);
//		FastDFAState sink = spec.addState(false);
//
//		spec.addTransition(s0, anyOutput, s4);
//		spec.addTransition(s0, chRsaLabel, s1);
//		spec.addTransition(s0, other, s1);
//		spec.addTransition(s1, other, s4);
//		spec.addTransition(s1, hvrLabel, s2);
//		spec.addTransition(s2, other, s4);
//		spec.addTransition(s2, chAny, s3);
//		spec.addTransition(s3, other, s4);
//		spec.addTransition(s3, shLabel, sink);
//		spec.addTransition(s4, other, s4);
//		spec.addTransition(sink, other, sink);
//
//
//		MessageLabel chRsa = new MessageLabel(true, "chrsa");
//		MessageLabel chPsk = new MessageLabel(true, "chpsk");
//		MessageLabel hvr = new MessageLabel(false, "hvr");
//		MessageLabel sh = new MessageLabel(false, "sh");
//		List<MessageLabel> messageLabels = Arrays.asList(chRsa, chPsk, hvr, sh);
//
//		FastDFA<MessageLabel> unfolded = DtlsLanguageAdapterBuilder.unfold(spec, messageLabels);
//		Assert.assertArrayEquals(messageLabels.toArray(), unfolded.getInputAlphabet().toArray());
//		Assert.assertTrue(unfolded.accepts(Arrays.asList(hvr)));
//		Assert.assertFalse(unfolded.accepts(Arrays.asList(chPsk)));
//	}
//
//	@Test
//	public void testUnfoldEnumeration() {
//		SymbolToken bTok = new SymbolToken(true, "b");
//		SymbolToken cTok = new SymbolToken(true, "c");
//		SymbolToken dTok = new SymbolToken(true, "d");
//		SpecificationLabel dLabel = new SpecificationLabel(dTok);
//		SpecificationLabel other = new SpecificationLabel(new OtherToken());
//		SpecificationLabel bOrC = new SpecificationLabel(new EnumerationToken(Arrays.asList(bTok, cTok)));
//		List<SpecificationLabel> specLabels = Arrays.asList(other, bOrC, dLabel);
//
//		FastDFA<SpecificationLabel> spec = new FastDFA<>(new SimpleAlphabet<>(specLabels));
//
//		FastDFAState s0 = spec.addInitialState(false);
//		FastDFAState s1 = spec.addState(false);
//		FastDFAState s2 = spec.addState(true);
//
//		spec.addTransition(s0, bOrC, s2);
//		spec.addTransition(s0, dLabel, s1);
//		spec.addTransition(s0, other, s0);
//		spec.addTransition(s1, other, s1);
//		spec.addTransition(s2, other, s2);
//
//
//		MessageLabel a = new MessageLabel(true, "a");
//		MessageLabel b = new MessageLabel(true, "b");
//		MessageLabel c = new MessageLabel(true, "c");
//		MessageLabel d = new MessageLabel(true, "d");
//
//		List<MessageLabel> messageLabels = Arrays.asList(a, b, c, d);
//
//		FastDFA<MessageLabel> expected = new FastDFA<>(new SimpleAlphabet<>(messageLabels));
//		s0 = expected.addInitialState(false);
//		s1 = expected.addState(false);
//		s2 = expected.addState(true);
//
//		expected.addTransition(s0, a, s0);
//		expected.addTransition(s0, b, s2);
//		expected.addTransition(s0, c, s2);
//		expected.addTransition(s0, d, s1);
//		expected.addTransition(s1, a, s1);
//		expected.addTransition(s1, b, s1);
//		expected.addTransition(s1, c, s1);
//		expected.addTransition(s1, d, s1);
//		expected.addTransition(s2, a, s2);
//		expected.addTransition(s2, b, s2);
//		expected.addTransition(s2, c, s2);
//		expected.addTransition(s2, d, s2);
//
//		FastDFA<MessageLabel> unfolded = DtlsLanguageAdapterBuilder.unfold(spec, messageLabels);
//		assertEquivalent(expected, unfolded, messageLabels, spec, specLabels);
//	}

//	@Test
//	public void testUnfoldOtherMinusEnumeration() {
//		SymbolToken bTok = new SymbolToken(true, "b");
//		SymbolToken cTok = new SymbolToken(true, "c");
//
//		SpecificationLabel otherThanC = new SpecificationLabel( new BinaryExpressionToken(new OtherToken(), BinarySetOperation.DIFFERENCE, cTok));
//		SpecificationLabel bLabel = new SpecificationLabel(bTok);
//		List<SpecificationLabel> specLabels = Arrays.asList(bLabel, otherThanC);
//
//		FastDFA<SpecificationLabel> spec = new FastDFA<>(new SimpleAlphabet<>(specLabels));
//
//		FastDFAState s0 = spec.addInitialState(false);
//		FastDFAState s1 = spec.addState(true);
//
//		spec.addTransition(s0, bLabel, s0);
//		spec.addTransition(s0, otherThanC, s1);
//
//		MessageLabel a = new MessageLabel(true, "a");
//		MessageLabel b = new MessageLabel(true, "b");
//		MessageLabel c = new MessageLabel(true, "c");
//
//		List<MessageLabel> messageLabels = Arrays.asList(a, b, c);
//
//		FastDFA<MessageLabel> expectedMut = new FastDFA<>(new SimpleAlphabet<>(messageLabels));
//		s0 = expectedMut.addInitialState(false);
//		s1 = expectedMut.addState(true);
//		expectedMut.addTransition(s0, a, s1);
//		expectedMut.addTransition(s0, b, s0);
//
//		DFA<?, MessageLabel> expected = TestUtils.completeDFA(expectedMut, messageLabels);
//		FastDFA<MessageLabel> unfolded = DtlsLanguageAdapterBuilder.unfold(spec, messageLabels);
//		assertEquivalent(expected, unfolded, messageLabels, spec, specLabels);
//	}
//
    @Test
    public void testUnionOther() {
        Symbol a = new InputSymbol("a");
        Symbol b = new InputSymbol("b");
        Symbol o1 = new OutputSymbol("o1");
        Symbol o2 = new OutputSymbol("o2");

        Label aOrO1 = new Label(new SetExpressionToken(new SymbolToken(a), SetOperator.UNION, new SymbolToken(o1)), new Guard(), new Update());
        Label otherOutput = new Label(new OtherToken(OtherTokenType.OUTPUT), new Guard(), new Update());
        Label otherInput = new Label(new OtherToken(OtherTokenType.INPUT), new Guard(), new Update());
        Label other = new Label(new OtherToken(OtherTokenType.ALL), new Guard(), new Update());

        List<Label> specLabels = Arrays.asList(aOrO1, otherOutput, otherInput, other);

        FastDFA<Label> spec = new FastDFA<>(new ListAlphabet<>(specLabels));

        FastDFAState s0 = spec.addInitialState(false);
        FastDFAState s1 = spec.addState(true);

        spec.addTransition(s0, aOrO1, s0);
        spec.addTransition(s0, otherOutput, s1);

        List<Symbol> symbols = Arrays.asList(a, b, o1, o2);
        Alphabet<Symbol> alphabet = new ListAlphabet<>(symbols);

        FastDFA<Symbol> expectedMut = new FastDFA<>(alphabet);
        s0 = expectedMut.addInitialState(false);
        s1 = expectedMut.addState(true);
        expectedMut.addTransition(s0, a, s0);
        expectedMut.addTransition(s0, o1, s0);
        expectedMut.addTransition(s0, o2, s1);

        DFA<?, Symbol> expectedDfa = TestUtils.completeDFA(expectedMut, alphabet);
        DefaultDFADecoder dfaDecoder = new DefaultDFADecoder();
        DFAAdapter actualDfa = dfaDecoder.decode(new EncodedDFA(spec, specLabels), symbols);

        assertEquivalent(expectedDfa, actualDfa.getDfa(), symbols, spec, specLabels);
    }
//
//	@Test
//	public void testBasicGuardAndAssignment() {
//		SymbolToken ch = new SymbolToken(true, "ClientHello");
//
//		Field kex = new Field("test") {
//
//			@Override
//			protected Value getValue(Symbol symbol) {
//				String kexString = symbol.name().substring(0, 3);
//				return new Value(kexString);
//			}
//		};
//
//		Variable a = new Variable("a");
//
//		Label chAssign = new Label(ch, Guard.trueGuard(), new Update(
//				new Assignment(a, kex)));
//		Label chEquals = new Label(ch, new Guard(new RelationalExpression(a, RelationalOperator.EQUAL, kex)), Update.emptyUpdate());
//		Label chNotEquals = new Label(ch, new Guard(new RelationalExpression(a, RelationalOperator.NOT_EQUAL, kex)), Update.emptyUpdate());
//		Label chTrue = new Label(ch);
//
//		List<Label> specLabels = Arrays.asList(chAssign, chEquals, chNotEquals, chTrue);
//
//		FastDFA<Label> spec = new FastDFA<>(new ListAlphabet<>(specLabels));
//
//		FastDFAState s0 = spec.addInitialState(false);
//		FastDFAState s1 = spec.addState(false);
//		FastDFAState s2 = spec.addState(true);
//		FastDFAState sink = spec.addState(false);
//
//		spec.addTransition(s0, chAssign, s1);
//		spec.addTransition(s1, chEquals, s2);
//		spec.addTransition(s1, chNotEquals, sink);
//		spec.addTransition(s2, chTrue, s2);
//		spec.addTransition(sink, chTrue, sink);
//
//		Symbol cha = new InputSymbol("PSK_CLIENT_HELLO");
//		Symbol chb = new InputSymbol("RSA_CLIENT_HELLO");
//		List<Symbol> symbols = Arrays.asList(cha, chb);
//
//		FastDFA<Symbol> expectedDfa = new FastDFA<>(new ListAlphabet<>(symbols));
//		s0 = expectedDfa.addInitialState(false);
//		s1 = expectedDfa.addState(false);
//		s2 = expectedDfa.addState(false);
//		FastDFAState s3 = expectedDfa.addState(true);
//		sink = expectedDfa.addState(false);
//
//		expectedDfa.addTransition(s0, cha, s1);
//		expectedDfa.addTransition(s0, chb, s2);
//		expectedDfa.addTransition(s1, cha, s3);
//		expectedDfa.addTransition(s1, chb, sink);
//		expectedDfa.addTransition(s2, cha, sink);
//		expectedDfa.addTransition(s2, chb, s3);
//		expectedDfa.addTransition(s3, cha, s3);
//		expectedDfa.addTransition(s3, chb, s3);
//		expectedDfa.addTransition(sink, cha, sink);
//		expectedDfa.addTransition(sink, chb, sink);
//
//		DefaultDfaDecoder dfaDecoder = new DefaultDfaDecoder();
//		DfaAdapter actualDfa = dfaDecoder.decode(new EncodedDfaHolder(spec, specLabels), symbols);
//
//		assertEquivalent(expectedDfa, actualDfa.getDfa(), symbols, spec, specLabels);
//	}

    private void assertEquivalent(DFA<?, Symbol> expected, DFA<?, Symbol> actual, Collection<Symbol> messageLabels, DFA<?, Label> spec, Collection<Label> specLabels) {
        Word<Symbol> sepWord = Automata.findSeparatingWord(expected, actual, messageLabels);
        Assert.assertNull(String.format("The DFA resulting from the unfolding of a test specification differs from what is expected. \n"
                + "Specification: %s\n Expected: %s\n Actual: %s\n SepWord: %s\n",
                TestUtils.getAutomataString(spec, specLabels),
                TestUtils.getAutomataString(expected, messageLabels),
                TestUtils.getAutomataString(actual, messageLabels),
                sepWord), sepWord);
    }
}

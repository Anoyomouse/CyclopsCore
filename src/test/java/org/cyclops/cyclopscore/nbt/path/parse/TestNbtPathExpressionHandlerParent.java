package org.cyclops.cyclopscore.nbt.path.parse;

import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import org.cyclops.cyclopscore.nbt.path.INbtPathExpression;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TestNbtPathExpressionHandlerParent {

    private NbtPathExpressionParseHandlerParent handler;

    @Before
    public void beforeEach() {
        handler = new NbtPathExpressionParseHandlerParent();
    }

    @Test
    public void testNonMatch() {
        assertThat(handler.handlePrefixOf("$", 0),
                is(INbtPathExpressionParseHandler.HandleResult.INVALID));
    }

    @Test
    public void testNonMatchShort() {
        assertThat(handler.handlePrefixOf(".", 0),
                is(INbtPathExpressionParseHandler.HandleResult.INVALID));
    }

    @Test
    public void testMatchSingle() {
        INbtPathExpressionParseHandler.HandleResult result = handler.handlePrefixOf("..", 0);
        assertThat(result.isValid(), is(true));
        assertThat(result.getConsumedExpressionLength(), is(2));
        assertThat(result.getPrefixExpression(), is(NbtPathExpressionParseHandlerParent.Expression.INSTANCE));
    }

    @Test
    public void testMatchSingleLater() {
        INbtPathExpressionParseHandler.HandleResult result = handler.handlePrefixOf("aa..", 2);
        assertThat(result.isValid(), is(true));
        assertThat(result.getConsumedExpressionLength(), is(2));
        assertThat(result.getPrefixExpression(), is(NbtPathExpressionParseHandlerParent.Expression.INSTANCE));
    }

    @Test
    public void testMatchLonger() {
        INbtPathExpressionParseHandler.HandleResult result = handler.handlePrefixOf("..aaa", 0);
        assertThat(result.isValid(), is(true));
        assertThat(result.getConsumedExpressionLength(), is(2));
        assertThat(result.getPrefixExpression(), is(NbtPathExpressionParseHandlerParent.Expression.INSTANCE));
    }

    @Test
    public void testExpressionStreamEmpty() {
        INbtPathExpression expression = NbtPathExpressionParseHandlerParent.Expression.INSTANCE;
        assertThat(expression.match(Stream.empty()).getMatches().collect(Collectors.toList()),
                is(Collections.emptyList()));
    }

    @Test
    public void testExpressionStreamNoParent() {
        INbtPathExpression expression = NbtPathExpressionParseHandlerParent.Expression.INSTANCE;
        assertThat(expression.match(Stream.of(new NBTTagString("a"))).getMatches().collect(Collectors.toList()),
                is(Lists.newArrayList()));
    }

    @Test
    public void testExpressionStreamSingle() {
        INbtPathExpression expression = NbtPathExpressionParseHandlerParent.Expression.INSTANCE;
        assertThat(expression.matchContexts(Stream.of(
                new NbtPathExpressionExecutionContext(new NBTTagString("a"),
                        new NbtPathExpressionExecutionContext(new NBTTagString("b"))
                )
                )).getMatches().collect(Collectors.toList()),
                is(Lists.newArrayList(new NBTTagString("b"))));
    }

    @Test
    public void testExpressionStreamMultiple() {
        INbtPathExpression expression = NbtPathExpressionParseHandlerParent.Expression.INSTANCE;
        Stream<NbtPathExpressionExecutionContext> stream = Stream.of(
                new NbtPathExpressionExecutionContext(new NBTTagString("a1"),
                        new NbtPathExpressionExecutionContext(new NBTTagString("a2"))
                ),
                new NbtPathExpressionExecutionContext(new NBTTagString("b1"),
                        new NbtPathExpressionExecutionContext(new NBTTagString("b2"))
                ),
                new NbtPathExpressionExecutionContext(new NBTTagString("c1"),
                        new NbtPathExpressionExecutionContext(new NBTTagString("c2"))
                )
        );
        assertThat(expression.matchContexts(stream).getMatches().collect(Collectors.toList()),
                is(Lists.newArrayList(
                        new NBTTagString("a2"),
                        new NBTTagString("b2"),
                        new NBTTagString("c2")
                )));
    }

    @Test
    public void testExpressionStartFromRoot() {
        INbtPathExpression expression = NbtPathExpressionParseHandlerParent.Expression.INSTANCE;
        Stream<NBTBase> stream = Stream.of(
                new NBTTagString("a"),
                new NBTTagString("b"),
                new NBTTagString("c")
        );
        assertThat(expression.match(stream).getMatches().collect(Collectors.toList()),
                is(Lists.newArrayList()));
    }

}

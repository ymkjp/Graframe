import groovy.transform.Canonical
import groovy.transform.TupleConstructor
import groovyjarjarantlr.collections.List
import junit.framework.TestCase
import org.junit.runner.RunWith

/**
 * 状態の定義は基本的にStateクラスのサブクラスで行う。
 * また、状態間で同じ名前のものがあれば、基本的に事前状態に動作が加わったものを引き継ぐようにフレームワークで吸収する。
 * invariantフィールドは、この状態で常に満たす必要がある条件を表している。
 * start, endフィールドはフラグで、定義する状態がシステムとして開始できる場所、終了できる場所であるかどうかを保持する。
 */
@TupleConstructor
@Canonical
class State
{
    String name
    Closure invariant
    boolean start
    boolean end
    def rightShift(Action action)
    {
        new TestCase(before:this, action:actoin)
    }

    @Override
    public String toString() {
        return name
    }

    void takeFrom(target)
    {
        this.properties.findAll{!(it.key =~ /[cC]lass/)}.each{this.properties[(it.key)] = it.value}
    }
}

class States
{
    def static final Empty = new State("リストが空の状態", {it.list.size() == 0}, true, false) {
        @Delegate
        List list = []
    }

    def static final HasSingle = new State("リストに要素が1つだけある状態", {it.list.size() == 1}, false, false) {
        @Delegate
        List list = []
    }

    def static final HasTwo = new State("リストに要素が2つだけある状態", {it.list.size == 2}, false, true) {
        @Delegate
        List list = []
    }
}

/**
 * 動作定義のコードのイメージ
 * FSMRunneerを指定したクラスがJUnit4の実行対象になるクラスである
 *
 * 各動作をegesフィールドに定義している
 * 例: リストの0番目では
 * - 事前動作: Empty
 * - 動作: Add.curry("Some Words")
 * - 事後状態: HasSingle
 * - 事後状態の条件: {after -> assert after[0] == "SomeWords"}
 *g
 * 実行の流れ:
 * 1. フレームワークがFSMRunnerが指定されているクラスのedgesフィールドからグラフを生成する
 * 2. フレームワークがたどるべきパスを数え上げて、テストケースとして登録する
 * 3. フレームワークがテストケースを順に実行する
 *  ① 事前状態のインスタンスを引数に動作を実行する
 *  ② 事後状態のインスタンスに事前状態のフィールドをコピーする
 *  ③ 事後状態の不変条件を実行し、失敗したらテストケースを終了させる
 *  ④ 事前状態と事後状態を引数に、事後状態で満たすべき条件を実行し、失敗したらテストケースを終了させる
 *
 * 基本的にはFSMRunnerがほとんどの処理を実行する。FSMRunnerの中でグラフ生成やパスの数え上げにJGraphTを使い、
 * グラフの頂点や辺になるデータや処理はテストコードとしてフレームワークのユーザーが実装する
 */
interface Action
{
    def run(before)
}

class Edges
{
    def static final Add = {word, before -> before.list += word}
    def static final Add2Times = {word, before -> 2.t gimes{before.list.add("$word$it")}}
    def static final Remove = {index, before -> before.list.remove(index)}
}

@RunWith(FSMRunner)
class Suite
{
    def edges = [
            Empty >> Add.curry("SomeWords") >> HasSingle << {after -> assert after[0] == "SomeWords"},
            HasSingle >> Add.curry("SecondSomeSomeWords") >> HasTwo << {after -> assert after[1] == "SecondSomeWords"},
            HasSingle >> Remove.curry(0) >> Empty,
            Empty >> Add2Times >> HasTwo << {after -> assert after.list == ["SomeWords0", "SomeWords1"]}
    ]
}

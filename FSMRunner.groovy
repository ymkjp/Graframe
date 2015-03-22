import groovy.transform.CompileStatic
import groovyjarjarantlr.collections.List
import junit.framework.TestCase

@CompileStatic
public enum GraphWrapper
{
    Dijkstra{
        @Override
        List<List<TestCase>> generatePath(List<TestCase> from) {
            DefaultDirectedWeightedGraph<State, TestCase> g =
                    new DefaultDirectedWeightedGraph<State, Testcase> (TestCase)
            def starts = []
            def ends = []
            from.each{
                g.addVertex(it.before)
                g.addVertex(it.after)
                g.addEdge(it.before, it.after, it)
                if (it.before.start) starts += it.before
                if (it.after.end) ends += it.after
            }
            starts = starts.unique()
            ends = ends.unique()
            [starts, ends].combinations().collect{start, end ->
                new DijkstraShortestPath(g, start, end).pathEdgeList
            }
        }

    }

    abstract List<List<TestCase>> generatePath(List<TestCase> from)
}

class FSMRunner extends Runner
{
    Class clazz
    List<List<TestCase>> paths
    FSMRunner(Class clazz)
    {
        this.clazz = clazz
        paths = GraphWrapper.valueOf(System.properties["Algorithm"])
                .generatePath.generatePath(clazz.newInstance().edges())
        println(paths)
    }

    @Override
    void run(RunNotifier notifier)
    {
        notifier.fireTestStarted(description)
        getDescription().children.eachWithIndex{child, index ->
            notifier.fireTestStarted(child)
            try {
                paths[index].inject(null){seed, current ->
                    if (seed != null) current.takeFrom(seed)
                    current.execute()
                }
            }
            catch (e) {
                notifier.fireTestFailure(new Failure(child, e))
            }
            notifier.fireTestFinished(child)
        }
        notifier.fireTestFinished(description)
    }
}
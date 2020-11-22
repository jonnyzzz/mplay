# Method Player

A utility project that helps to
record a set of method calls to a given 
object or interface into a file. 

Next it is able to playback the same method call
sequence independently of the rest of the application.


## Use Cases

Profiling the whole application may not allow to see the slowdowns
of a given part of the application. Using the __Method Player__ we
may record the interesting method calls and later re-play this
part of the application independently under a profiler. 

Comparing implementations can be tricky too. Thanks to this utility
one may capture the production usage patterns and use it to compare
the performance of both implementations based on real use-cases.

Trick usage patterns can be hard to reproduce in tests. Thanks to the
code-generation feature, the record can be re-played into a code. The
code can be included into a test scenario.

## License

This tool is available under Apache 2.0 license, see [LICENSE](LICENSE)
for more details.


# javalib
lib in pure java

similar to [jslib](https://github.com/beenotung/jslib), [myutils](https://github.com/beenotung/myutils)
but this repo contains only java code
minimal dependency on external library
require jdk8 to work, please ...
(not only the functionality in Stream, but the lambda expression is almost a must...)

## Requirement
 - linux
    1. git
 - windows
    1. cygwin (cat, grep, sed, git)
    or install [Git for Windows](https://git-scm.com/) (include the unix tools)

## Install
1. copy the file **inject_lib.sh** under the user project
2. update the variable *main_file* in **inject_lib.sh** if necessary
3. run the script **inject_lib.sh**

## Library Format
The code are kept in a single file, in case there is requirement like 'the homework should be done in a single java file'

Mainly think in Scala style, most of the 'class' are actually 'object' (only consist of static functions)

## Short Term Goal
Some of the feature are implemented in scala, but what if you are not allowed to use scala (e.g. in assignment)

This library is very compact, (for quick reimplement during competition when e-resources is not allowed to bring in)

## Long Term Goal
Maybe it's more worth to work with Scala (https://github.com/beenotung/myutils)

## Style
1. Avoid writing method
e.g. ```list.update(f)```
better write in ```update(list,f)```
This can be syntactically painful when using the functions, but this way can work on the most type of data (It is not like Javascript, cannot 'polyfill' an existing Class (cannot modify the standary library))
2. Write monadic method
To be easy to do chained operation (e.g. map)
But only when the class is purely not depending on it's parent
3. As abstract as possible in the parameter
e.g. take Collection instead of List if possible

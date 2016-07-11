Bottom-Up Parser
================

Sample output for input of `(())`:
```
    == chart 0
      S -> P . from 0
      P -> . from 0 // from closure
      P -> . ( P ) from 0 // from closure
      S -> . P from 0
    == chart 1
      P -> ( P . ) from 0
      P -> . from 1
      P -> . ( P ) from 1
      P -> ( . P ) from 0
    == chart 2
      P -> ( P . ) from 1
      P -> . from 2
      P -> . ( P ) from 2
      P -> ( . P ) from 1
    == chart 3
      P -> ( P . ) from 0
      P -> ( P ) . from 1
    == chart 4
      S -> P . from 0
      P -> ( P ) . from 0
```

Closure
=======
`x -> a b . c d, j`

for each grammar c -> p q r, where c's match
make a next state c -> . p q r, i
if c is a terminal, then bring in
those production rules

Shift
=====
`x -> a b . c d, j`

if tokens(i) == c then
make next state x -> a b c . d, j
in shift(i + 1). Parse c if the next
token is equal to c and move to j+1

Reduce
======
`x -> a b . c d, j`

If cd is empty and state is x -> a b . , j
for each p -> q . x r, l in chart(j)
make a new state p -> q x . r , l in chart(i)
We just parsed an "x" that may have been a sub-step
like matching "exp -> 2" in "2 + 3". We should update the
higher-level rules as well.

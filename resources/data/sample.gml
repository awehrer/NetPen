graph [
	comment "This is a sample graph"
	directed 1
	id 42
	label "Hello, I am a graph"
	node [
		id 1
		label "node 1"
		thisIsASampleAttribute 42
		awesomeness 0.1
		abc "1"
	]
	node [
		id 2
		label "node 2"
		thisIsASampleAttribute 1
		abc "2"
	]
	node [
		id 3
		label "node 3"
		thisIsASampleAttribute 44
		awesomeness 0.6
		abc "3"
	]
	node [
		id 4
		label "node 3"
		thisIsASampleAttribute 44
		awesomeness 1.1
		abc "4"
	]
	node [
		id 5
		label "node 3"
		thisIsASampleAttribute 44
		awesomeness 0.05
		abc "5"
	]
	node [
		id 6
		label "node 3"
		thisIsASampleAttribute 44
		awesomeness 0.8
		abc "6"
	]
	node [
		id 7
		label "node 3"
		thisIsASampleAttribute 44
		awesomeness 0.333
		abc "7"
	]
	edge [
		source 1
		target 2
		label "Edge from node 1 to node 2"
	]
	edge [
		source 2
		target 3
		label "Edge from node 2 to node 3"
	]
	edge [
		source 3
		target 1
		label "Edge from node 3 to node 1"
	]
	edge [
		source 7
		target 1
		label "Edge from node 3 to node 1"
	]
	edge [
		source 3
		target 5
		label "Edge from node 3 to node 1"
	]
]
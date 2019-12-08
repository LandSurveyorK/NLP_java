ant -f build_assign_parsing.xml
export DATA_PATH="/Users/pengwei/javaMaster/algorithms_for_nlp/project2/wsj"
java -cp assign_parsing.jar:assign_parsing-submit.jar -server -mx2000m edu.berkeley.nlp.assignments.parsing.PCFGParserTester -path $DATA_PATH -parserType COARSE_TO_FINE -maxTrainLength 15 -maxTestLength 15 -test -quiet

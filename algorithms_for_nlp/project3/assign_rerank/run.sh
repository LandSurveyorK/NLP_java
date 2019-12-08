ant -f build_assign_rerank.xml
export DATA_PATH="/Users/pengwei/javaMaster/algorithms_for_nlp/project3/rerank-data"
java -cp assign_rerank.jar:assign_rerank-submit.jar -server -mx6000m edu.berkeley.nlp.assignments.rerank.ParsingRerankerTester -path $DATA_PATH -rerankerType AWESOME -test  

ant -f build_assign_align.xml
export DATA_PATH="/Users/pengwei/javaMaster/algorithms_for_nlp/project4/align_data"
java -cp assign_align.jar:assign_align-submit.jar -server -Xmx8g edu.berkeley.nlp.assignments.align.AlignmentTester -path  $DATA_PATH -alignerType HMM -data test -phraseTableOut -phraseTable.txt -maxTrain 10000 -noprint

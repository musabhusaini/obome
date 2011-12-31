/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package featureextraction;

import java.sql.*;

/**
 *
 * @author bgulcu
 */
public class FELib {
    public Statement get_db_connection(String jdriver, String url, String user_name, String password) throws ClassNotFoundException, SQLException {
	//mysql
        Class.forName(jdriver);
        Connection conn = DriverManager.getConnection(url,user_name,password);
        Statement stmt = conn.createStatement();
	return stmt;
    }
    public void initialize(Statement insert_stmt) throws SQLException{
        insert_stmt.execute("update tripadvisor.review_1000 set is_evaluated = 0");
        //insert_stmt.execute('truncate table tripadvisor.extracted_features')
    }
    public int get_sentiment_score(String review_content){
        return 1;
    }
    
    // TODO keyword ve aspect geçmiyorsa çıkma ihtimalini artır 
    // TODO bir önceki review'da kullanılan keyword'lerin olduğu review'ları sona at
    // TODO next'e basıldıktan sonra is_evaluated = 1 olsun.
    public String get_next_sample(Statement stmt, Statement insert_stmt) throws SQLException{
        String review = new String();
        int review_id = -1;
	String query =  "select content, review_id \n";
	query += "from tripadvisor.review_1000 \n";
	query += "where sentiment_eff_measure > 9.1 \n";
	query += "and is_evaluated = 0 \n";
	query += "order by word_freq_eff_measure/(length(content)-length(replace(content, ' ', ''))) desc, sentiment_eff_measure desc, review_id";
	ResultSet result_set = stmt.executeQuery(query);
	while(result_set.next()){
            review = result_set.getString(1);
            review_id = result_set.getInt(2);
        }
	this.mark_evaluation_flag(insert_stmt, review_id);
	return review;
    }
    public void mark_evaluation_flag(Statement insert_stmt, int review_id) throws SQLException{
        insert_stmt.execute("update tripadvisor.review_1000 set is_evaluated = 1 where review_id = " + review_id);
    }
    public void add_aspect(Statement stmt, String aspect){
        
    }
    public void add_keywords(Statement stmt, int aspect_id, String[] keyword_list){
        
    }
    public void get_input(){
	System.out.println("#########");
	System.out.println("# n: next");
	System.out.println("# q: quit");
	System.out.println("# a: aspect list");
	System.out.println("# f: aspect:feature list");
	System.out.println("# aspect;keyword1 keyword2 keyword3");
	System.out.println("#########");
	//return raw_input("> ");
    }
    public void get_plain_input(){
        //return raw_input("> ");
    }
    
    // TODO: fix user message. Should output "xxx aspect has been added"/"xxx aspect has been updated" and similar with keywords
    public void parse_input(Statement insert_stmt, String user_input) throws SQLException{
	int separator = user_input.indexOf(";");
	String aspect = user_input.substring(0, separator);
	String keyword_lst[] = user_input.substring(separator+1, user_input.length()).split(" ");
	for(int i=0; i<keyword_lst.length; i++){
            insert_stmt.execute("insert into tripadvisor.extracted_features values ('" + aspect + "', '" + Integer.toString(i) + "')");
        }
	System.out.println("Inserted aspect and the keywords");
    }
    public String[] get_aspects(Statement stmt) throws SQLException{
	String aspect_lst[] = new String[100];
	String query =  "select distinct aspect \n";
	query += "from tripadvisor.extracted_features \n";
	query += "order by 1";
	ResultSet result_set = stmt.executeQuery(query);
        int i = 0;
	while(result_set.next()){
            aspect_lst[i] = result_set.getString(1);
            i++;
        }
	return aspect_lst;
    }
    public String[] get_keywords(Statement stmt, String aspect) throws SQLException{
	String keyword_lst[] = new String[100];
	String query =  "select distinct keyword \n";
	query += "from tripadvisor.extracted_features \n";
	query += "where aspect = '" + aspect + "' \n";
	query += "order by 1";
        int i = 0;
	ResultSet result_set = stmt.executeQuery(query);
	while(result_set.next()){
            keyword_lst[i] = result_set.getString(1);
            i++;
        }
	return keyword_lst;
    }
    public String[][] get_features(Statement stmt) throws SQLException{
	String feature_lst[][] = new String[100][2];
	String query =  "select aspect, keyword \n";
	query += "from tripadvisor.extracted_features \n";
	query += "order by 1, 2";
        int i = 0;
	ResultSet result_set = stmt.executeQuery(query);
	while(result_set.next()){
            feature_lst[i][0] = result_set.getString(1);
            feature_lst[i][1] = result_set.getString(2);
            i++;
        }
	return feature_lst;
    }
}

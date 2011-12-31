/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.sabanciuniv.dataMining.featureExtraction;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


        
/**
 *
 * @author bgulcu
 */
public class FeatureExtractionProgram {    
    public Statement stmt;
    public Statement insert_stmt;
    public FELib lib_fe = new FELib();
    
    public DefaultListModel keyword_data = new DefaultListModel();
    public DefaultListModel aspect_data = new DefaultListModel();
    public String selected_aspect;
    
    public JTextField aspect_field;
    public JTextField keyword_field;
    public JList aspect_list;
    public JList keyword_list;
    public JLabel review_label;
    
    public FeatureExtractionProgram(Statement stmt, Statement insert_stmt, FELib lib_fe) throws SQLException{
        this.stmt = stmt;
        this.insert_stmt = insert_stmt;
        this.lib_fe = lib_fe;
        this.selected_aspect = "";
        
        createUI();
    }
    
    public final void createUI() throws SQLException{
        //frames
        JFrame main_frame = create_main_frame();
        //aspects
        JScrollPane aspect_spane = create_aspect_spane();
        JScrollPane keyword_spane = create_keyword_spane();
        //input fields
        this.aspect_field = new JTextField("aspect",5);
        this.keyword_field = new JTextField("keyword1 keyword2",5);
        //label
        JScrollPane review_spane = create_review_spane();

        //# buttons
        JButton button_list[] = new JButton[2];
        button_list = create_buttons();
        //bundan sonrasini da ayri function'lar olarak bol
        //review_label SPane'in içindeydi ama biz gidip ayrı JPanel'a koymuşuz.
        //bunun kararını vermek lazım.
        //# panels
        //# button panel
        JPanel btn_panel = new JPanel();
        btn_panel.add(button_list[0]);
        btn_panel.add(button_list[1]);
        //# list panel
        JPanel lst_panel = new JPanel();
        lst_panel.add(aspect_spane);
        lst_panel.add(keyword_spane);
        //# text field panel
        JPanel inp_panel = new JPanel(new BorderLayout(10, 0));
        inp_panel.add(this.aspect_field,BorderLayout.WEST);
        inp_panel.add(this.keyword_field,BorderLayout.CENTER);
        //# review panel
        JPanel review_panel = new JPanel(new BorderLayout(0, 10));
        review_panel.add(this.review_label,BorderLayout.CENTER);
        review_panel.add(inp_panel,BorderLayout.SOUTH);
        
        //# place panels in frame
        main_frame.add(btn_panel,BorderLayout.SOUTH);
        main_frame.add(lst_panel,BorderLayout.EAST);
        main_frame.add(review_panel,BorderLayout.WEST);

        main_frame.pack();
        main_frame.setVisible(true);
        
    }
    public JButton[] create_buttons() {
        JButton btn_lst[] = new JButton[2];
        btn_lst[0] = new JButton("Add");
        btn_lst[0].addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
                try {
                    i_add_aspect(ae);
                } catch (SQLException ex) {
                    Logger.getLogger(FeatureExtractionProgram.class.getName()).log(Level.SEVERE, null, ex);
                }
			}
        });
        //# next button
        btn_lst[1] = new JButton("Next");
        btn_lst[1].addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                try {
                    i_get_next_sample(ae);
                } catch (SQLException ex) {
                    Logger.getLogger(FeatureExtractionProgram.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        return btn_lst;
    }
    public JScrollPane create_review_spane() throws SQLException {
        JScrollPane review_spane = new JScrollPane();
        this.review_label = new JLabel("<html>" + lib_fe.get_next_sample(stmt, insert_stmt) + "</html>");
	this.review_label.setPreferredSize(new Dimension(1000, 550));
	this.review_label.setHorizontalAlignment(JLabel.LEFT);
	this.review_label.setVerticalAlignment(JLabel.TOP);
        return review_spane;
    }
    public JScrollPane create_aspect_spane() throws SQLException{
        this.aspect_data = new DefaultListModel();
        String aspect_string_list[] = lib_fe.get_aspects(this.stmt);
        for(int i = 0; i < aspect_string_list.length; i++)
            this.aspect_data.addElement(aspect_string_list[i]);
        this.aspect_list = new JList(this.aspect_data);
        
        //listener'i ayarlayamadim...
        this.aspect_list.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent lse) {
                try {
                    //throw new UnsupportedOperationException("Not supported yet.");
                    set_aspect(lse);
                } catch (SQLException ex) {
                    Logger.getLogger(FeatureExtractionProgram.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        JScrollPane aspect_spane = new JScrollPane();
        aspect_spane.setPreferredSize(new Dimension(100,500));
        aspect_spane.getViewport().setView((this.aspect_list));
        return aspect_spane;
    }
    public JScrollPane create_keyword_spane() throws SQLException{
        this.keyword_data = new DefaultListModel();
        String keyword_string_list[] = lib_fe.get_keywords(this.stmt, this.selected_aspect);
        for(int i = 0; i < keyword_string_list.length; i++)
            this.keyword_data.addElement(keyword_string_list[i]);
        this.keyword_list = new JList(this.keyword_data);
        
        JScrollPane keyword_spane = new JScrollPane();
        keyword_spane.setPreferredSize(new Dimension(100,500));
        keyword_spane.getViewport().setView((this.aspect_list));
        return keyword_spane;
    }
    public JFrame create_main_frame(){
        JFrame frame = new JFrame("Feature Extraction");
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        return frame;
    }
    
    public void refresh_lists(int refresh_type) throws SQLException{
        if(refresh_type == 1){
            this.keyword_data.clear();
            String aspect_string_list[] = lib_fe.get_aspects(this.stmt);
            for(int i = 0; i < aspect_string_list.length; i++)
                this.aspect_data.addElement(aspect_string_list[i]);
        } else if(refresh_type == 2){
            this.keyword_data.clear();
            String keyword_string_list[] = lib_fe.get_keywords(this.stmt, this.selected_aspect);
            for(int i = 0; i < keyword_string_list.length; i++)
                this.keyword_data.addElement(keyword_string_list[i]);
        } else {
            this.aspect_data.clear();
            String aspect_string_list[] = lib_fe.get_aspects(this.stmt);
            for(int i = 0; i < aspect_string_list.length; i++)
                this.aspect_data.addElement(aspect_string_list[i]);
            this.keyword_data.clear();
            String keyword_string_list[] = lib_fe.get_keywords(this.stmt, this.selected_aspect);
            for(int i = 0; i < keyword_string_list.length; i++)
                this.keyword_data.addElement(keyword_string_list[i]);
        }
    }
    
    // TODO eğer daha önce aspect eklenmişse, yenisini ekleme.
    public void i_add_aspect(ActionEvent event) throws SQLException{
        //#self.selected_aspect = ""
        //#self.aspect_list.clearSelection();
        lib_fe.parse_input(this.insert_stmt, this.aspect_field.getText() + ";" + this.keyword_field.getText());
        if(this.aspect_data.indexOf(this.aspect_field.getText()) == -1)
            this.aspect_data.addElement(this.aspect_field.getText());
        this.aspect_field.setText("");
        this.keyword_field.setText("");
    }
    public void i_get_next_sample(ActionEvent event) throws SQLException{
        this.review_label.setText("<html>" + lib_fe.get_next_sample(this.stmt, this.insert_stmt) + "</html>");
        //#print lib_fe.get_next_sample(self.stmt, self.insert_stmt);
    }
    public void set_aspect(ListSelectionEvent event) throws SQLException{
        this.selected_aspect = (String) this.aspect_data.elementAt(this.aspect_list.getSelectedIndex());
        //#print self.aspect_data.elementAt(self.aspect_list.selectedIndex)
        refresh_lists(2);    
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        // TODO code application logic here
        System.out.println("helpme!");
        
        FELib lib_fe = new FELib();
        String jdriver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://localhost:3306/tripadvisor";
		String user_name = "java_user";
		String password = "java_user_pwd";

        Statement stmt = lib_fe.get_db_connection(jdriver, url, user_name, password);
        Statement insert_stmt = lib_fe.get_db_connection(jdriver, url, user_name, password);
        FeatureExtractionProgram slf = new FeatureExtractionProgram(stmt, insert_stmt, lib_fe);
    }
}
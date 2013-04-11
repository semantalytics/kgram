
import fr.inria.acacia.corese.exceptions.EngineException;
import fr.inria.edelweiss.kgdqp.core.QueryProcessDQP;
import fr.inria.edelweiss.kgdqp.core.WSImplem;
import fr.inria.edelweiss.kgram.core.Mappings;
import fr.inria.edelweiss.kgraph.core.Graph;
import fr.inria.edelweiss.kgtool.print.XMLFormat;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingWorker;
import org.apache.commons.lang.time.StopWatch;
import wsimport.KgramWS.RemoteProducer;
import wsimport.KgramWS.RemoteProducerServiceClient;

/**
 *
 * @author johan
 */
public class ExpeCredibleFrame extends javax.swing.JFrame {
    private static final String DATADIR = System.getProperty("user.home") + "/projets/credible/demonstration/data/";
    
    //private static RemoteProducer kg1 = null;
    private static RemoteProducer kg2 = null;
    private static RemoteProducer kg3 = null;
    private static RemoteProducer kg4 = null;
    
    /**
     * Creates new form ExpeCredibleFrame
     */
    public ExpeCredibleFrame() {
        initComponents();
        prefixesTextArea.setText("PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
                + "PREFIX dataset: <http://www.irisa.fr/visages/team/farooq/ontologies/dataset-owl-lite.owl#>\n"
                + "PREFIX study: <http://www.irisa.fr/visages/team/farooq/ontologies/study-owl-lite.owl#>\n"
                + "PREFIX DBIOL: <http://www.irisa.fr/visages/team/farooq/ontologies/database-integration-owl-lite.owl#>\n"
                + "PREFIX human: <http://www.irisa.fr/visages/team/farooq/ontologies/human-owl-lite.owl#>\n"
                + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>\n"
                + "PREFIX iec: <http://www.irisa.fr/visages/team/farooq/ontologies/iec-owl-lite.owl#>\n"
                + "PREFIX examination-subject: <http://www.irisa.fr/visages/team/farooq/ontologies/examination-subject-owl-lite.owl#>\n"
                + "PREFIX property: <http://neurolex.org/wiki/Special:URIResolver/Property-3A>\n"
                + "PREFIX linguistic-expression: <http://www.irisa.fr/visages/team/farooq/ontologies/linguistic-expression-owl-lite.owl#>\n");
        prefixesTextArea.setCaretPosition(0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        checkBoxIRISA = new javax.swing.JCheckBox();
        checkBoxIFR49 = new javax.swing.JCheckBox();
        checkBoxNLEX = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        prefixesTextArea = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        queryTextArea = new javax.swing.JTextArea();
        queryButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        resultsTextArea = new javax.swing.JTextArea();
        queriesComboBox = new javax.swing.JComboBox();
        numberOfResultsLabel = new javax.swing.JLabel();
        timeLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Repositories");

        checkBoxIRISA.setText("IRISA repository");

        checkBoxIFR49.setText("IFR49 repository");

        checkBoxNLEX.setText("NLEX ontology");

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(checkBoxIRISA, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(checkBoxIFR49, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 836, Short.MAX_VALUE)
            .add(checkBoxNLEX, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(checkBoxIRISA)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(checkBoxIFR49)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(checkBoxNLEX)
                .add(0, 0, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 78, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jLabel1)
                .add(0, 0, Short.MAX_VALUE))
            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jLabel2.setText("Query");

        jLabel3.setText("Prefixes");

        prefixesTextArea.setColumns(20);
        prefixesTextArea.setRows(5);
        jScrollPane1.setViewportView(prefixesTextArea);

        queryTextArea.setColumns(20);
        queryTextArea.setRows(5);
        jScrollPane2.setViewportView(queryTextArea);

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 78, Short.MAX_VALUE)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1)
                    .add(jScrollPane2)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jLabel3)
                        .add(0, 0, Short.MAX_VALUE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 135, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        queryButton.setText("Execute query");
        queryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queryButtonActionPerformed(evt);
            }
        });

        resultsTextArea.setColumns(20);
        resultsTextArea.setRows(5);
        jScrollPane3.setViewportView(resultsTextArea);

        queriesComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Empty query", "MRI gadolinium contrasted images", "T1-weighted MR images", "NeuroLEX-labelled Diffusion Tensor Images" }));
        queriesComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                queriesComboBoxActionPerformed(evt);
            }
        });

        numberOfResultsLabel.setText(" ");

        timeLabel.setText(" ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(numberOfResultsLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(timeLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane3)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(queriesComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 708, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(queryButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(queryButton)
                    .add(queriesComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(numberOfResultsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(timeLabel)
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void queriesComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queriesComboBoxActionPerformed
        switch(queriesComboBox.getSelectedIndex()) {
            case 1:
                queryTextArea.setText("SELECT DISTINCT ?patient ?study ?dataset ?dsName WHERE { \n"
                        + "     ?patient iec:is-referred-to-by/linguistic-expression:has-for-name ?dsName . \n"
                        //+ "     ?patient iec:is-referred-to-by ?dataset  .\n"
                        //+ "     ?dataset linguistic-expression:has-for-name ?dsName .\n"
                        + "     ?patient examination-subject:has-for-subject-identifier ?clinID .\n"
                        + "     ?study study:involves-as-patient ?patient .\n"
                        + "     FILTER ((?clinID ~ 'MS') && (?dsName ~ 'GADO'))\n"
                        + "}\n");
                break;
            case 2:
                queryTextArea.setText("SELECT DISTINCT ?subject ?dataset ?name WHERE { \n"
                + "     ?dataset linguistic-expression:has-for-name ?name . \n"
                + "     ?subject iec:is-referred-to-by ?dataset . \n"
                + "     FILTER (?name ~ 'T1') \n"
                + "} \n");
                break;
            case 3:
                queryTextArea.setText("SELECT DISTINCT ?patient ?clinID ?datasetName ?nlex_label WHERE { \n"
                //+ "     ?t property:Label \"MRI protocol\"^^xsd:string . \n"
                + "     ?t property:Label \"Diffusion magnetic resonance imaging protocol\"^^xsd:string . \n"
                + "     ?s rdfs:subClassOf* ?t . \n"
                + "     ?s property:Label ?nlex_label . \n"
                + "     ?dataset property:Label ?nlex_label . \n"
                + "     ?dataset linguistic-expression:has-for-name ?datasetName . \n"
                + "     ?patient iec:is-referred-to-by ?dataset . \n"
                + "     ?patient examination-subject:has-for-subject-identifier ?clinID . \n"
                + "} \n");
                break;
            default:
                queryTextArea.setText("");
                break;
        }
        queryTextArea.setCaretPosition(0);
    }//GEN-LAST:event_queriesComboBoxActionPerformed

    private void queryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_queryButtonActionPerformed
        resultsTextArea.setText("");
        timeLabel.setText(" ");
        numberOfResultsLabel.setText(" ");
        
        new SwingWorker() {
            @Override
            public Object doInBackground() {
                Graph graph = Graph.create();

                QueryProcessDQP exec = QueryProcessDQP.create(graph);
                try {
                    //exec.addRemote(new URL("http://localhost:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"));
                    if(checkBoxIRISA.isSelected()) {
                        exec.addRemote(new URL("http://localhost:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
                    }
                    if(checkBoxIFR49.isSelected()) {
                        exec.addRemote(new URL("http://localhost:8093/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
                    }
                    if(checkBoxNLEX.isSelected()) {
                        exec.addRemote(new URL("http://localhost:8094/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort"), WSImplem.SOAP);
                    }
                }
                catch(MalformedURLException e) {
                }

                StopWatch sw = new StopWatch();
                sw.start();
                try {
                    Mappings maps = exec.query(prefixesTextArea.getText() + queryTextArea.getText());

                    timeLabel.setText(sw.getTime() + " ms");
                    numberOfResultsLabel.setText(maps.size() + " results");
                    resultsTextArea.setText(XMLFormat.create(maps).toString());
                }
                catch(EngineException e) {
                    resultsTextArea.setText(e.getMessage());
                    timeLabel.setText(" ");
                    numberOfResultsLabel.setText(" ");
                }
                resultsTextArea.setCaretPosition(0);
                return null;
            }
        }.execute();
        
    }//GEN-LAST:event_queryButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ExpeCredibleFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ExpeCredibleFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ExpeCredibleFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ExpeCredibleFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ExpeCredibleFrame().setVisible(true);
            }
        });
        
        ExecutorService executor = Executors.newCachedThreadPool();
        /*executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    kg1 = RemoteProducerServiceClient.getPort("http://localhost:8091/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
                    kg1.initEngine();
                    kg1.loadRDF(DATADIR + "linkedData-source-i3s.rdf");
                }
                catch(MalformedURLException e) {
                }
            }
        });*/
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    kg2 = RemoteProducerServiceClient.getPort("http://localhost:8092/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
                    kg2.initEngine();
                    kg2.loadRDF(DATADIR + "linkedData-source-irisa.rdf");
                }
                catch(MalformedURLException e) {
                }
            }
        });
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    kg3 = RemoteProducerServiceClient.getPort("http://localhost:8093/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
                    kg3.initEngine();
                    kg3.loadRDF(DATADIR + "linkedData-source-ifr49.rdf");
                }
                catch(MalformedURLException e) {
                }                
            }
        });
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    kg4 = RemoteProducerServiceClient.getPort("http://localhost:8094/kgserver-1.0.7-kgram-webservice/RemoteProducerService.RemoteProducerServicePort");
                    kg4.initEngine();
                    kg4.loadRDF(DATADIR + "nlx_stage_all.owl");
                    kg4.loadRDF(DATADIR + "bridgeNeuroLEX.rdf");        
                }
                catch(MalformedURLException e) {
                }                
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox checkBoxIFR49;
    private javax.swing.JCheckBox checkBoxIRISA;
    private javax.swing.JCheckBox checkBoxNLEX;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel numberOfResultsLabel;
    private javax.swing.JTextArea prefixesTextArea;
    private javax.swing.JComboBox queriesComboBox;
    private javax.swing.JButton queryButton;
    private javax.swing.JTextArea queryTextArea;
    private javax.swing.JTextArea resultsTextArea;
    private javax.swing.JLabel timeLabel;
    // End of variables declaration//GEN-END:variables
}

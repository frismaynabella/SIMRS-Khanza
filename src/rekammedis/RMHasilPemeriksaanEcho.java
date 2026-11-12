package rekammedis;

import bridging.ApiOrthanc;
import bridging.OrthancDICOM;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import kepegawaian.DlgCariDokter;
import java.nio.file.Files;
import java.util.Base64;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;


/**
 *
 * @author perpustakaan
 */
public final class RMHasilPemeriksaanEcho extends javax.swing.JDialog {
    private final DefaultTableModel tabMode,tabModeDicom;
    private Connection koneksi=koneksiDB.condb();
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i=0;
    private DlgCariDokter dokter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private StringBuilder htmlContent;
    private String finger="";
    private JsonNode root;
    private String TANGGALMUNDUR="yes";
    private ObjectMapper mapper= new ObjectMapper();
    
    /** Creates new form DlgRujuk
     * @param parent
     * @param modal */
    public RMHasilPemeriksaanEcho(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        // CUSTOM HASIL PEMERIKSAAN ECHO - Table Model dengan 45 kolom (7 metadata + 9 original + 18 history + 11 spectral)
        tabMode=new DefaultTableModel(null,new Object[]{
                "No.Rawat","No.RM","Nama Pasien","Tgl.Lahir","Kode Dokter","Nama Dokter","Tanggal","F Sistolik LV","F Diastolic LV",
                "Kontraktilitas RV","Dimensi Ruang Jantung","Katup-katup","Analisa Segmental","eRAP","Lain-lain","Kesimpulan",
                "Aorta","Left Atrium","RV Diastolic","Ejection Fraction","EPSS","LVOT Diameter",
                "LV EDD","LV ESD","IVS Diastolic","LVPW Diastolic","TAPSE","RVOT",
                "Aortic Sinus","Asc Aorta","Aortic Arcus","Desc Aorta","Abdominal Aorta","RVOT Diameter",
                "Dec Time","E/A Ratio","TVG","m PAP","E'max","Ao Vmax",
                "Ao VTI","TD LV E/e'","TD LV E/a'","IVC Diameter","IVC RaP"
            }){
              @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };

        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // CUSTOM HASIL PEMERIKSAAN ECHO - Set column width untuk 45 kolom
        for (i = 0; i < 45; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(105);
            }else if(i==1){
                column.setPreferredWidth(70);
            }else if(i==2){
                column.setPreferredWidth(150);
            }else if(i==3){
                column.setPreferredWidth(65);
            }else if(i==4){
                column.setPreferredWidth(80);
            }else if(i==5){
                column.setPreferredWidth(150);
            }else if(i==6){
                column.setPreferredWidth(115);
            }else if(i==7){
                column.setPreferredWidth(150);
            }else if(i==8){
                column.setPreferredWidth(150);
            }else if(i==9){
                column.setPreferredWidth(130);
            }else if(i==10){
                column.setPreferredWidth(150);
            }else if(i==11){
                column.setPreferredWidth(150);
            }else if(i==12){
                column.setPreferredWidth(150);
            }else if(i==13){
                column.setPreferredWidth(150);
            }else if(i==14){
                column.setPreferredWidth(180);
            }else if(i==15){
                column.setPreferredWidth(250);
            }else if(i>=16 && i<=33){
                // CUSTOM HASIL PEMERIKSAAN ECHO - 18 kolom HISTORY/CLINICAL DIAGNOSIS (width 100px)
                column.setPreferredWidth(100);
            }else if(i>=34 && i<=44){
                // CUSTOM HASIL PEMERIKSAAN ECHO - 11 kolom SPECTRAL DOPPLER (width 100px)
                column.setPreferredWidth(100);
            }
        }
        
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        
        tabModeDicom=new DefaultTableModel(null,new Object[]{
            "UUID Pasien","ID Studies","ID Series"}){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbListDicom.setModel(tabModeDicom);
        tbListDicom.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbListDicom.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 3; i++) {
            TableColumn column = tbListDicom.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(100);
            }else if(i==1){
                column.setPreferredWidth(270);
            }else if(i==2){
                column.setPreferredWidth(270);
            }
        }
        tbListDicom.setDefaultRenderer(Object.class, new WarnaTable());
        
        TNoRw.setDocument(new batasInput((byte)17).getKata(TNoRw));
        Sistolik.setDocument(new batasInput((int)30).getKata(Sistolik));
        Diastolik.setDocument(new batasInput((int)30).getKata(Diastolik));
        Kontraktilitas.setDocument(new batasInput((int)30).getKata(Kontraktilitas));
        Dimensi.setDocument(new batasInput((int)50).getKata(Dimensi));
        Katup.setDocument(new batasInput((int)50).getKata(Katup));
        AnalisaSegmental.setDocument(new batasInput((int)100).getKata(AnalisaSegmental));
        Erap.setDocument(new batasInput((int)15).getKata(Erap));
        LainLain.setDocument(new batasInput((int)100).getKata(LainLain));
        Kesimpulan.setDocument(new batasInput((int)200).getKata(Kesimpulan));
        TCari.setDocument(new batasInput((int)100).getKata(TCari));
        
        ChkAccor.setSelected(false);
        isPhoto();
        
        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditable(true);
        LoadHTML.setEditorKit(kit);
        LoadHTML2.setEditable(false);
        LoadHTML2.addHyperlinkListener(e -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
              Desktop desktop = Desktop.getDesktop();
              try {
                desktop.browse(e.getURL().toURI());
              } catch (Exception ex) {
                ex.printStackTrace();
              }
            }
        });
        LoadHTML2.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
                ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi2 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#323232;}"+
                ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                ".isi5 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#AA0000;}"+
                ".isi6 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#FF0000;}"+
                ".isi7 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#C8C800;}"+
                ".isi8 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#00AA00;}"+
                ".isi9 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#969696;}"
        );
        Document doc = kit.createDefaultDocument();
        LoadHTML.setDocument(doc);
        LoadHTML2.setDocument(doc);
        
        try {
            TANGGALMUNDUR=koneksiDB.TANGGALMUNDUR();
        } catch (Exception e) {
            TANGGALMUNDUR="yes";
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LoadHTML = new widget.editorpane();
        jPopupMenu1 = new widget.PopupMenu();
        MnPenilaianMedis = new widget.MenuItem();
        TanggalRegistrasi = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        BtnAll = new widget.Button();
        BtnKeluar = new widget.Button();
        TabRawat = new widget.TabPane();
        internalFrame2 = new widget.InternalFrame();
        scrollInput = new widget.ScrollPane();
        FormInput = new widget.PanelBiasa();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        TNoRM = new widget.TextBox();
        label14 = new widget.Label();
        KdDokter = new widget.TextBox();
        NmDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        jLabel10 = new widget.Label();
        jSeparator1 = new javax.swing.JSeparator();
        label11 = new widget.Label();
        Tanggal = new widget.Tanggal();
        jLabel31 = new widget.Label();
        Sistolik = new widget.TextBox();
        Katup = new widget.TextBox();
        jLabel35 = new widget.Label();
        jLabel40 = new widget.Label();
        Diastolik = new widget.TextBox();
        jLabel41 = new widget.Label();
        AnalisaSegmental = new widget.TextBox();
        Dimensi = new widget.TextBox();
        jLabel43 = new widget.Label();
        jLabel44 = new widget.Label();
        scrollPane17 = new widget.ScrollPane();
        Kesimpulan = new widget.TextArea();
        jLabel45 = new widget.Label();
        Erap = new widget.TextBox();
        jLabel42 = new widget.Label();
        Kontraktilitas = new widget.TextBox();
        jLabel46 = new widget.Label();
        LainLain = new widget.TextBox();
        internalFrame3 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        PanelAccor = new widget.PanelBiasa();
        ChkAccor = new widget.CekBox();
        TabData = new widget.TabPane();
        FormPhoto = new widget.PanelBiasa();
        FormPass3 = new widget.PanelBiasa();
        btnAmbil = new widget.Button();
        BtnRefreshPhoto1 = new widget.Button();
        Scroll5 = new widget.ScrollPane();
        LoadHTML2 = new widget.editorpane();
        FormOrthan = new widget.PanelBiasa();
        Scroll6 = new widget.ScrollPane();
        tbListDicom = new widget.Table();
        panelGlass7 = new widget.panelisi();
        btnDicom = new widget.Button();
        btnUploud = new widget.Button();

        LoadHTML.setBorder(null);
        LoadHTML.setName("LoadHTML"); // NOI18N

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnPenilaianMedis.setBackground(new java.awt.Color(255, 255, 254));
        MnPenilaianMedis.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnPenilaianMedis.setForeground(new java.awt.Color(50, 50, 50));
        MnPenilaianMedis.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnPenilaianMedis.setText("Formulir Hasil Pemeriksaan ECHOCARDIOGRAFI");
        MnPenilaianMedis.setName("MnPenilaianMedis"); // NOI18N
        MnPenilaianMedis.setPreferredSize(new java.awt.Dimension(320, 26));
        MnPenilaianMedis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnPenilaianMedisActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnPenilaianMedis);

        TanggalRegistrasi.setHighlighter(null);
        TanggalRegistrasi.setName("TanggalRegistrasi"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Hasil Pemeriksaan ECHOCARDIOGRAFI ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setPreferredSize(new java.awt.Dimension(467, 500));
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 54));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan"); // NOI18N
        BtnSimpan.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanActionPerformed(evt);
            }
        });
        BtnSimpan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpanKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnSimpan);

        BtnBatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png"))); // NOI18N
        BtnBatal.setMnemonic('B');
        BtnBatal.setText("Baru");
        BtnBatal.setToolTipText("Alt+B");
        BtnBatal.setName("BtnBatal"); // NOI18N
        BtnBatal.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnBatalActionPerformed(evt);
            }
        });
        BtnBatal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnBatalKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnBatal);

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus"); // NOI18N
        BtnHapus.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnHapusActionPerformed(evt);
            }
        });
        BtnHapus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnHapusKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnHapus);

        BtnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnEdit.setMnemonic('G');
        BtnEdit.setText("Ganti");
        BtnEdit.setToolTipText("Alt+G");
        BtnEdit.setName("BtnEdit"); // NOI18N
        BtnEdit.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnEditActionPerformed(evt);
            }
        });
        BtnEdit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnEditKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnEdit);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setName("BtnPrint"); // NOI18N
        BtnPrint.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintActionPerformed(evt);
            }
        });
        BtnPrint.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPrintKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnPrint);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllActionPerformed(evt);
            }
        });
        BtnAll.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAllKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnAll);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnKeluarKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnKeluar);

        internalFrame1.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        TabRawat.setBackground(new java.awt.Color(254, 255, 254));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.setPreferredSize(new java.awt.Dimension(457, 480));

        internalFrame2.setBorder(null);
        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setPreferredSize(new java.awt.Dimension(102, 480));
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        scrollInput.setName("scrollInput"); // NOI18N
        scrollInput.setPreferredSize(new java.awt.Dimension(102, 557));

        FormInput.setBackground(new java.awt.Color(255, 255, 255));
        FormInput.setBorder(null);
        FormInput.setName("FormInput"); // NOI18N
        // CUSTOM HASIL PEMERIKSAAN ECHO - Update height untuk mengakomodasi SPECTRAL DOPPLER + DESCRIPTION
        FormInput.setPreferredSize(new java.awt.Dimension(750, 995));
        FormInput.setLayout(null);

        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(74, 10, 131, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        FormInput.add(TPasien);
        TPasien.setBounds(309, 10, 260, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        FormInput.add(TNoRM);
        TNoRM.setBounds(207, 10, 100, 23);

        label14.setText("Dokter :");
        label14.setName("label14"); // NOI18N
        label14.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label14);
        label14.setBounds(0, 40, 70, 23);

        KdDokter.setEditable(false);
        KdDokter.setName("KdDokter"); // NOI18N
        KdDokter.setPreferredSize(new java.awt.Dimension(80, 23));
        FormInput.add(KdDokter);
        KdDokter.setBounds(74, 40, 110, 23);

        NmDokter.setEditable(false);
        NmDokter.setName("NmDokter"); // NOI18N
        NmDokter.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(NmDokter);
        NmDokter.setBounds(186, 40, 295, 23);

        BtnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDokter.setMnemonic('2');
        BtnDokter.setToolTipText("Alt+2");
        BtnDokter.setName("BtnDokter"); // NOI18N
        BtnDokter.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnDokter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnDokterActionPerformed(evt);
            }
        });
        BtnDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnDokterKeyPressed(evt);
            }
        });
        FormInput.add(BtnDokter);
        BtnDokter.setBounds(484, 40, 28, 23);

        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(580, 10, 60, 23);

        TglLahir.setEditable(false);
        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        FormInput.add(TglLahir);
        TglLahir.setBounds(644, 10, 80, 23);

        jLabel10.setText("No.Rawat :");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(0, 10, 70, 23);

        jSeparator1.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator1.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator1.setName("jSeparator1"); // NOI18N
        FormInput.add(jSeparator1);
        jSeparator1.setBounds(0, 70, 750, 1);

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(70, 23));
        FormInput.add(label11);
        label11.setBounds(538, 40, 52, 23);

        Tanggal.setForeground(new java.awt.Color(50, 70, 50));
        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08-04-2026 14:52:55" }));
        Tanggal.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.setOpaque(false);
        Tanggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKeyPressed(evt);
            }
        });
        FormInput.add(Tanggal);
        Tanggal.setBounds(594, 40, 130, 23);

        jLabel31.setText("Fungsi Sistolik LV :");
        jLabel31.setName("jLabel31"); // NOI18N
        FormInput.add(jLabel31);
        jLabel31.setBounds(0, 660, 135, 23);

        Sistolik.setFocusTraversalPolicyProvider(true);
        Sistolik.setName("Sistolik"); // NOI18N
        Sistolik.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SistolikKeyPressed(evt);
            }
        });
        FormInput.add(Sistolik);
        Sistolik.setBounds(139, 660, 585, 23);

        Katup.setFocusTraversalPolicyProvider(true);
        Katup.setName("Katup"); // NOI18N
        Katup.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KatupKeyPressed(evt);
            }
        });
        FormInput.add(Katup);
        Katup.setBounds(139, 780, 585, 23);

        jLabel35.setText("Katup-katup :");
        jLabel35.setName("jLabel35"); // NOI18N
        FormInput.add(jLabel35);
        jLabel35.setBounds(0, 780, 135, 23);

        jLabel40.setText("Fungsi Diastolik LV :");
        jLabel40.setName("jLabel40"); // NOI18N
        FormInput.add(jLabel40);
        jLabel40.setBounds(0, 690, 135, 23);

        Diastolik.setFocusTraversalPolicyProvider(true);
        Diastolik.setName("Diastolik"); // NOI18N
        Diastolik.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DiastolikKeyPressed(evt);
            }
        });
        FormInput.add(Diastolik);
        Diastolik.setBounds(139, 690, 585, 23);

        jLabel41.setText("Analisa Segmental :");
        jLabel41.setName("jLabel41"); // NOI18N
        jLabel41.setPreferredSize(new java.awt.Dimension(68, 14));
        FormInput.add(jLabel41);
        jLabel41.setBounds(0, 810, 135, 23);

        AnalisaSegmental.setFocusTraversalPolicyProvider(true);
        AnalisaSegmental.setName("AnalisaSegmental"); // NOI18N
        AnalisaSegmental.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AnalisaSegmentalKeyPressed(evt);
            }
        });
        FormInput.add(AnalisaSegmental);
        AnalisaSegmental.setBounds(139, 810, 585, 23);

        Dimensi.setFocusTraversalPolicyProvider(true);
        Dimensi.setName("Dimensi"); // NOI18N
        Dimensi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DimensiKeyPressed(evt);
            }
        });
        FormInput.add(Dimensi);
        Dimensi.setBounds(139, 750, 585, 23);

        jLabel43.setText("Dimensi Ruang Jantung :");
        jLabel43.setName("jLabel43"); // NOI18N
        FormInput.add(jLabel43);
        jLabel43.setBounds(0, 750, 135, 23);

        jLabel44.setText("Kesimpulan :");
        jLabel44.setName("jLabel44"); // NOI18N
        FormInput.add(jLabel44);
        jLabel44.setBounds(0, 900, 135, 23);

        scrollPane17.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane17.setName("scrollPane17"); // NOI18N

        Kesimpulan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Kesimpulan.setColumns(20);
        Kesimpulan.setRows(5);
        Kesimpulan.setName("Kesimpulan"); // NOI18N
        Kesimpulan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KesimpulanKeyPressed(evt);
            }
        });
        scrollPane17.setViewportView(Kesimpulan);

        FormInput.add(scrollPane17);
        scrollPane17.setBounds(139, 900, 585, 73);

        jLabel45.setText("eRAP :");
        jLabel45.setName("jLabel45"); // NOI18N
        jLabel45.setPreferredSize(new java.awt.Dimension(68, 14));
        FormInput.add(jLabel45);
        jLabel45.setBounds(0, 840, 135, 23);

        Erap.setFocusTraversalPolicyProvider(true);
        Erap.setName("Erap"); // NOI18N
        Erap.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ErapKeyPressed(evt);
            }
        });
        FormInput.add(Erap);
        Erap.setBounds(139, 840, 585, 23);

        jLabel42.setText("Kontaktilitas RV :");
        jLabel42.setName("jLabel42"); // NOI18N
        FormInput.add(jLabel42);
        jLabel42.setBounds(0, 720, 135, 23);

        Kontraktilitas.setFocusTraversalPolicyProvider(true);
        Kontraktilitas.setName("Kontraktilitas"); // NOI18N
        Kontraktilitas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KontraktilitasKeyPressed(evt);
            }
        });
        FormInput.add(Kontraktilitas);
        Kontraktilitas.setBounds(139, 720, 585, 23);

        jLabel46.setText("Lain-lain :");
        jLabel46.setName("jLabel46"); // NOI18N
        jLabel46.setPreferredSize(new java.awt.Dimension(68, 14));
        FormInput.add(jLabel46);
        jLabel46.setBounds(0, 870, 135, 23);

        LainLain.setFocusTraversalPolicyProvider(true);
        LainLain.setName("LainLain"); // NOI18N
        LainLain.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LainLainKeyPressed(evt);
            }
        });
        FormInput.add(LainLain);
        LainLain.setBounds(139, 870, 585, 23);

        // CUSTOM HASIL PEMERIKSAAN ECHO - HISTORY/CLINICAL DIAGNOSIS - Separator 1
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator2.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator2.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator2.setName("jSeparator2"); // NOI18N
        FormInput.add(jSeparator2);
        jSeparator2.setBounds(0, 70, 750, 1);

        // CUSTOM HASIL PEMERIKSAAN ECHO - HISTORY/CLINICAL DIAGNOSIS - Header Label
        jLabelHistoryClinical = new widget.Label();
        jLabelHistoryClinical.setText("HISTORY/CLINICAL DIAGNOSIS");
        jLabelHistoryClinical.setName("jLabelHistoryClinical"); // NOI18N
        jLabelHistoryClinical.setFont(new java.awt.Font("Tahoma", 1, 11)); // Bold
        FormInput.add(jLabelHistoryClinical);
        jLabelHistoryClinical.setBounds(0, 75, 750, 23);

        // CUSTOM HASIL PEMERIKSAAN ECHO - HISTORY/CLINICAL DIAGNOSIS - Sub-header Label
        jLabel2DMode = new widget.Label();
        jLabel2DMode.setText("2D/M Mode 31");
        jLabel2DMode.setName("jLabel2DMode"); // NOI18N
        jLabel2DMode.setFont(new java.awt.Font("Tahoma", 1, 11)); // Bold
        jLabel2DMode.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        FormInput.add(jLabel2DMode);
        jLabel2DMode.setBounds(0, 100, 750, 23);

        // CUSTOM HASIL PEMERIKSAAN ECHO - HISTORY/CLINICAL DIAGNOSIS - KOLOM 1 (Kiri)
        // 1. Aorta
        jLabelAorta = new widget.Label();
        jLabelAorta.setText("Aorta (20-30 mm) :");
        jLabelAorta.setName("jLabelAorta"); // NOI18N
        FormInput.add(jLabelAorta);
        jLabelAorta.setBounds(0, 130, 140, 23);

        Aorta = new widget.TextBox();
        Aorta.setName("Aorta"); // NOI18N
        Aorta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AortaKeyPressed(evt);
            }
        });
        FormInput.add(Aorta);
        Aorta.setBounds(145, 130, 100, 23);

        // 2. Left Atrium
        jLabelLeftAtrium = new widget.Label();
        jLabelLeftAtrium.setText("Left Atrium (20-40 mm) :");
        jLabelLeftAtrium.setName("jLabelLeftAtrium"); // NOI18N
        FormInput.add(jLabelLeftAtrium);
        jLabelLeftAtrium.setBounds(0, 160, 140, 23);

        LeftAtrium = new widget.TextBox();
        LeftAtrium.setName("LeftAtrium"); // NOI18N
        LeftAtrium.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LeftAtriumKeyPressed(evt);
            }
        });
        FormInput.add(LeftAtrium);
        LeftAtrium.setBounds(145, 160, 100, 23);

        // 3. RV Diastolic
        jLabelRVDiastolic = new widget.Label();
        jLabelRVDiastolic.setText("RV Diastolic (11-24 mm) :");
        jLabelRVDiastolic.setName("jLabelRVDiastolic"); // NOI18N
        FormInput.add(jLabelRVDiastolic);
        jLabelRVDiastolic.setBounds(0, 190, 140, 23);

        RVDiastolic = new widget.TextBox();
        RVDiastolic.setName("RVDiastolic"); // NOI18N
        RVDiastolic.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RVDiastolicKeyPressed(evt);
            }
        });
        FormInput.add(RVDiastolic);
        RVDiastolic.setBounds(145, 190, 100, 23);

        // 4. Ejection Fraction
        jLabelEjectionFraction = new widget.Label();
        jLabelEjectionFraction.setText("Ejection Fraction (53-77%) :");
        jLabelEjectionFraction.setName("jLabelEjectionFraction"); // NOI18N
        FormInput.add(jLabelEjectionFraction);
        jLabelEjectionFraction.setBounds(0, 220, 140, 23);

        EjectionFraction = new widget.TextBox();
        EjectionFraction.setName("EjectionFraction"); // NOI18N
        EjectionFraction.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                EjectionFractionKeyPressed(evt);
            }
        });
        FormInput.add(EjectionFraction);
        EjectionFraction.setBounds(145, 220, 100, 23);

        // 5. EPSS
        jLabelEPSS = new widget.Label();
        jLabelEPSS.setText("EPSS (0-6 mm) :");
        jLabelEPSS.setName("jLabelEPSS"); // NOI18N
        FormInput.add(jLabelEPSS);
        jLabelEPSS.setBounds(0, 250, 140, 23);

        EPSS = new widget.TextBox();
        EPSS.setName("EPSS"); // NOI18N
        EPSS.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                EPSSKeyPressed(evt);
            }
        });
        FormInput.add(EPSS);
        EPSS.setBounds(145, 250, 100, 23);

        // 6. LVOT Diameter
        jLabelLVOTDiameter = new widget.Label();
        jLabelLVOTDiameter.setText("LVOT Diameter (17-23 mm) :");
        jLabelLVOTDiameter.setName("jLabelLVOTDiameter"); // NOI18N
        FormInput.add(jLabelLVOTDiameter);
        jLabelLVOTDiameter.setBounds(0, 280, 140, 23);

        LVOTDiameter = new widget.TextBox();
        LVOTDiameter.setName("LVOTDiameter"); // NOI18N
        LVOTDiameter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LVOTDiameterKeyPressed(evt);
            }
        });
        FormInput.add(LVOTDiameter);
        LVOTDiameter.setBounds(145, 280, 100, 23);

        // CUSTOM HASIL PEMERIKSAAN ECHO - HISTORY/CLINICAL DIAGNOSIS - KOLOM 2 (Tengah)
        // 7. LV EDD
        jLabelLVEDD = new widget.Label();
        jLabelLVEDD.setText("LV EDD (37-55 mm) :");
        jLabelLVEDD.setName("jLabelLVEDD"); // NOI18N
        FormInput.add(jLabelLVEDD);
        jLabelLVEDD.setBounds(250, 130, 140, 23);

        LVEDD = new widget.TextBox();
        LVEDD.setName("LVEDD"); // NOI18N
        LVEDD.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LVEDDKeyPressed(evt);
            }
        });
        FormInput.add(LVEDD);
        LVEDD.setBounds(395, 130, 100, 23);

        // 8. LV ESD
        jLabelLVESD = new widget.Label();
        jLabelLVESD.setText("LV ESD (22-39 mm) :");
        jLabelLVESD.setName("jLabelLVESD"); // NOI18N
        FormInput.add(jLabelLVESD);
        jLabelLVESD.setBounds(250, 160, 140, 23);

        LVESD = new widget.TextBox();
        LVESD.setName("LVESD"); // NOI18N
        LVESD.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LVESDKeyPressed(evt);
            }
        });
        FormInput.add(LVESD);
        LVESD.setBounds(395, 160, 100, 23);

        // 9. IVS Diastolic
        jLabelIVSDiastolic = new widget.Label();
        jLabelIVSDiastolic.setText("IVS Diastolic (6-12 mm) :");
        jLabelIVSDiastolic.setName("jLabelIVSDiastolic"); // NOI18N
        FormInput.add(jLabelIVSDiastolic);
        jLabelIVSDiastolic.setBounds(250, 190, 140, 23);

        IVSDiastolic = new widget.TextBox();
        IVSDiastolic.setName("IVSDiastolic"); // NOI18N
        IVSDiastolic.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                IVSDiastolicKeyPressed(evt);
            }
        });
        FormInput.add(IVSDiastolic);
        IVSDiastolic.setBounds(395, 190, 100, 23);

        // 10. LVPW Diastolic
        jLabelLVPWDiastolic = new widget.Label();
        jLabelLVPWDiastolic.setText("LVPW Diastolic (6-12 mm) :");
        jLabelLVPWDiastolic.setName("jLabelLVPWDiastolic"); // NOI18N
        FormInput.add(jLabelLVPWDiastolic);
        jLabelLVPWDiastolic.setBounds(250, 220, 140, 23);

        LVPWDiastolic = new widget.TextBox();
        LVPWDiastolic.setName("LVPWDiastolic"); // NOI18N
        LVPWDiastolic.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LVPWDiastolicKeyPressed(evt);
            }
        });
        FormInput.add(LVPWDiastolic);
        LVPWDiastolic.setBounds(395, 220, 100, 23);

        // 11. TAPSE
        jLabelTAPSE = new widget.Label();
        jLabelTAPSE.setText("TAPSE (15-30 mm) :");
        jLabelTAPSE.setName("jLabelTAPSE"); // NOI18N
        FormInput.add(jLabelTAPSE);
        jLabelTAPSE.setBounds(250, 250, 140, 23);

        TAPSE = new widget.TextBox();
        TAPSE.setName("TAPSE"); // NOI18N
        TAPSE.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TAPSEKeyPressed(evt);
            }
        });
        FormInput.add(TAPSE);
        TAPSE.setBounds(395, 250, 100, 23);

        // 12. RVOT
        jLabelRVOT = new widget.Label();
        jLabelRVOT.setText("RVOT (20-30 mm) :");
        jLabelRVOT.setName("jLabelRVOT"); // NOI18N
        FormInput.add(jLabelRVOT);
        jLabelRVOT.setBounds(250, 280, 140, 23);

        RVOT = new widget.TextBox();
        RVOT.setName("RVOT"); // NOI18N
        RVOT.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RVOTKeyPressed(evt);
            }
        });
        FormInput.add(RVOT);
        RVOT.setBounds(395, 280, 100, 23);

        // CUSTOM HASIL PEMERIKSAAN ECHO - HISTORY/CLINICAL DIAGNOSIS - KOLOM 3 (Kanan)
        // 13. Aortic Sinus
        jLabelAorticSinus = new widget.Label();
        jLabelAorticSinus.setText("Aortic Sinus (20-40 mm) :");
        jLabelAorticSinus.setName("jLabelAorticSinus"); // NOI18N
        FormInput.add(jLabelAorticSinus);
        jLabelAorticSinus.setBounds(500, 130, 150, 23);

        AorticSinus = new widget.TextBox();
        AorticSinus.setName("AorticSinus"); // NOI18N
        AorticSinus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AorticSinusKeyPressed(evt);
            }
        });
        FormInput.add(AorticSinus);
        AorticSinus.setBounds(655, 130, 100, 23);

        // 14. Asc Aorta
        jLabelAscAorta = new widget.Label();
        jLabelAscAorta.setText("Asc Aorta (20-40 mm) :");
        jLabelAscAorta.setName("jLabelAscAorta"); // NOI18N
        FormInput.add(jLabelAscAorta);
        jLabelAscAorta.setBounds(500, 160, 150, 23);

        AscAorta = new widget.TextBox();
        AscAorta.setName("AscAorta"); // NOI18N
        AscAorta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AscAortaKeyPressed(evt);
            }
        });
        FormInput.add(AscAorta);
        AscAorta.setBounds(655, 160, 100, 23);

        // 15. Aortic Arcus
        jLabelAorticArcus = new widget.Label();
        jLabelAorticArcus.setText("Aortic Arcus (18-40 mm) :");
        jLabelAorticArcus.setName("jLabelAorticArcus"); // NOI18N
        FormInput.add(jLabelAorticArcus);
        jLabelAorticArcus.setBounds(500, 190, 150, 23);

        AorticArcus = new widget.TextBox();
        AorticArcus.setName("AorticArcus"); // NOI18N
        AorticArcus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AorticArcusKeyPressed(evt);
            }
        });
        FormInput.add(AorticArcus);
        AorticArcus.setBounds(655, 190, 100, 23);

        // 16. Desc Aorta
        jLabelDescAorta = new widget.Label();
        jLabelDescAorta.setText("Desc Aorta (18-30 mm) :");
        jLabelDescAorta.setName("jLabelDescAorta"); // NOI18N
        FormInput.add(jLabelDescAorta);
        jLabelDescAorta.setBounds(500, 220, 150, 23);

        DescAorta = new widget.TextBox();
        DescAorta.setName("DescAorta"); // NOI18N
        DescAorta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DescAortaKeyPressed(evt);
            }
        });
        FormInput.add(DescAorta);
        DescAorta.setBounds(655, 220, 100, 23);

        // 17. Abdominal Aorta
        jLabelAbdominalAorta = new widget.Label();
        jLabelAbdominalAorta.setText("Abdominal Aorta (15-30 mm) :");
        jLabelAbdominalAorta.setName("jLabelAbdominalAorta"); // NOI18N
        FormInput.add(jLabelAbdominalAorta);
        jLabelAbdominalAorta.setBounds(500, 250, 150, 23);

        AbdominalAorta = new widget.TextBox();
        AbdominalAorta.setName("AbdominalAorta"); // NOI18N
        AbdominalAorta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AbdominalAortaKeyPressed(evt);
            }
        });
        FormInput.add(AbdominalAorta);
        AbdominalAorta.setBounds(655, 250, 100, 23);

        // 18. RVOT Diameter
        jLabelRVOTDiameter = new widget.Label();
        jLabelRVOTDiameter.setText("RVOT Diameter (17-23 mm) :");
        jLabelRVOTDiameter.setName("jLabelRVOTDiameter"); // NOI18N
        FormInput.add(jLabelRVOTDiameter);
        jLabelRVOTDiameter.setBounds(500, 280, 150, 23);

        RVOTDiameter = new widget.TextBox();
        RVOTDiameter.setName("RVOTDiameter"); // NOI18N
        RVOTDiameter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RVOTDiameterKeyPressed(evt);
            }
        });
        FormInput.add(RVOTDiameter);
        RVOTDiameter.setBounds(655, 280, 100, 23);

        // CUSTOM HASIL PEMERIKSAAN ECHO - HISTORY/CLINICAL DIAGNOSIS - Separator 2 (penutup)
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator3.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator3.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator3.setName("jSeparator3"); // NOI18N
        FormInput.add(jSeparator3);
        jSeparator3.setBounds(0, 310, 750, 1);
        // END CUSTOM HASIL PEMERIKSAAN ECHO - HISTORY/CLINICAL DIAGNOSIS

        // CUSTOM HASIL PEMERIKSAAN ECHO - SPECTRAL DOPPLER - Header
        jLabelSpectralDoppler = new widget.Label();
        jLabelSpectralDoppler.setText("SPECTRAL DOPPLER");
        jLabelSpectralDoppler.setName("jLabelSpectralDoppler"); // NOI18N
        jLabelSpectralDoppler.setFont(new java.awt.Font("Tahoma", 1, 11)); // Bold
        FormInput.add(jLabelSpectralDoppler);
        jLabelSpectralDoppler.setBounds(0, 315, 750, 23);

        // CUSTOM HASIL PEMERIKSAAN ECHO - SPECTRAL DOPPLER - KOLOM KIRI
        // Sub-header: Mitral Inflow
        jLabelMitralInflow = new widget.Label();
        jLabelMitralInflow.setText("Mitral Inflow");
        jLabelMitralInflow.setName("jLabelMitralInflow"); // NOI18N
        jLabelMitralInflow.setFont(new java.awt.Font("Tahoma", 1, 11)); // Bold
        FormInput.add(jLabelMitralInflow);
        jLabelMitralInflow.setBounds(0, 340, 180, 23);

        // 1. Dec Time
        jLabelDecTime = new widget.Label();
        jLabelDecTime.setText("Dec Time (m/sec) :");
        jLabelDecTime.setName("jLabelDecTime"); // NOI18N
        FormInput.add(jLabelDecTime);
        jLabelDecTime.setBounds(0, 365, 180, 23);

        DecTime = new widget.TextBox();
        DecTime.setName("DecTime"); // NOI18N
        DecTime.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DecTimeKeyPressed(evt);
            }
        });
        FormInput.add(DecTime);
        DecTime.setBounds(185, 365, 100, 23);

        // 2. E/A
        jLabelEARatio = new widget.Label();
        jLabelEARatio.setText("E/A :");
        jLabelEARatio.setName("jLabelEARatio"); // NOI18N
        FormInput.add(jLabelEARatio);
        jLabelEARatio.setBounds(0, 395, 180, 23);

        EARatio = new widget.TextBox();
        EARatio.setName("EARatio"); // NOI18N
        EARatio.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                EARatioKeyPressed(evt);
            }
        });
        FormInput.add(EARatio);
        EARatio.setBounds(185, 395, 100, 23);

        // Sub-header: Tricuspid Regurgitation
        jLabelTricuspidRegurgitation = new widget.Label();
        jLabelTricuspidRegurgitation.setText("Tricuspid Regurgitation");
        jLabelTricuspidRegurgitation.setName("jLabelTricuspidRegurgitation"); // NOI18N
        jLabelTricuspidRegurgitation.setFont(new java.awt.Font("Tahoma", 1, 11)); // Bold
        FormInput.add(jLabelTricuspidRegurgitation);
        jLabelTricuspidRegurgitation.setBounds(0, 425, 200, 23);

        // 3. TVG
        jLabelTVG = new widget.Label();
        jLabelTVG.setText("TVG (mmHg) :");
        jLabelTVG.setName("jLabelTVG"); // NOI18N
        FormInput.add(jLabelTVG);
        jLabelTVG.setBounds(0, 450, 180, 23);

        TVG = new widget.TextBox();
        TVG.setName("TVG"); // NOI18N
        TVG.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TVGKeyPressed(evt);
            }
        });
        FormInput.add(TVG);
        TVG.setBounds(185, 450, 100, 23);

        // Sub-header: Pulmonary
        jLabelPulmonary = new widget.Label();
        jLabelPulmonary.setText("Pulmonary");
        jLabelPulmonary.setName("jLabelPulmonary"); // NOI18N
        jLabelPulmonary.setFont(new java.awt.Font("Tahoma", 1, 11)); // Bold
        FormInput.add(jLabelPulmonary);
        jLabelPulmonary.setBounds(0, 480, 180, 23);

        // 4. m PAP
        jLabelMPAP = new widget.Label();
        jLabelMPAP.setText("m PAP (mmHg) :");
        jLabelMPAP.setName("jLabelMPAP"); // NOI18N
        FormInput.add(jLabelMPAP);
        jLabelMPAP.setBounds(0, 505, 180, 23);

        MPAP = new widget.TextBox();
        MPAP.setName("MPAP"); // NOI18N
        MPAP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MPAPKeyPressed(evt);
            }
        });
        FormInput.add(MPAP);
        MPAP.setBounds(185, 505, 100, 23);

        // CUSTOM HASIL PEMERIKSAAN ECHO - SPECTRAL DOPPLER - KOLOM KANAN
        // Sub-header: Tissue Doppler
        jLabelTissueDoppler = new widget.Label();
        jLabelTissueDoppler.setText("Tissue Doppler");
        jLabelTissueDoppler.setName("jLabelTissueDoppler"); // NOI18N
        jLabelTissueDoppler.setFont(new java.awt.Font("Tahoma", 1, 11)); // Bold
        FormInput.add(jLabelTissueDoppler);
        jLabelTissueDoppler.setBounds(400, 340, 180, 23);

        // 5. E'max
        jLabelEmax = new widget.Label();
        jLabelEmax.setText("E'max (m/s) :");
        jLabelEmax.setName("jLabelEmax"); // NOI18N
        FormInput.add(jLabelEmax);
        jLabelEmax.setBounds(400, 365, 180, 23);

        Emax = new widget.TextBox();
        Emax.setName("Emax"); // NOI18N
        Emax.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                EmaxKeyPressed(evt);
            }
        });
        FormInput.add(Emax);
        Emax.setBounds(585, 365, 100, 23);

        // 6. Ao Vmax
        jLabelAoVmax = new widget.Label();
        jLabelAoVmax.setText("Ao Vmax (m/s) :");
        jLabelAoVmax.setName("jLabelAoVmax"); // NOI18N
        FormInput.add(jLabelAoVmax);
        jLabelAoVmax.setBounds(400, 395, 180, 23);

        AoVmax = new widget.TextBox();
        AoVmax.setName("AoVmax"); // NOI18N
        AoVmax.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AoVmaxKeyPressed(evt);
            }
        });
        FormInput.add(AoVmax);
        AoVmax.setBounds(585, 395, 100, 23);

        // 7. Ao VTI
        jLabelAoVTI = new widget.Label();
        jLabelAoVTI.setText("Ao VTI (cm) :");
        jLabelAoVTI.setName("jLabelAoVTI"); // NOI18N
        FormInput.add(jLabelAoVTI);
        jLabelAoVTI.setBounds(400, 425, 180, 23);

        AoVTI = new widget.TextBox();
        AoVTI.setName("AoVTI"); // NOI18N
        AoVTI.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AoVTIKeyPressed(evt);
            }
        });
        FormInput.add(AoVTI);
        AoVTI.setBounds(585, 425, 100, 23);

        // Sub-header: Tissue Doppler LV
        jLabelTissueDopplerLV = new widget.Label();
        jLabelTissueDopplerLV.setText("Tissue Doppler LV");
        jLabelTissueDopplerLV.setName("jLabelTissueDopplerLV"); // NOI18N
        jLabelTissueDopplerLV.setFont(new java.awt.Font("Tahoma", 1, 11)); // Bold
        FormInput.add(jLabelTissueDopplerLV);
        jLabelTissueDopplerLV.setBounds(400, 455, 180, 23);

        // 8. E/E'
        jLabelTDLVEE = new widget.Label();
        jLabelTDLVEE.setText("E/E' :");
        jLabelTDLVEE.setName("jLabelTDLVEE"); // NOI18N
        FormInput.add(jLabelTDLVEE);
        jLabelTDLVEE.setBounds(400, 480, 180, 23);

        TDLVEE = new widget.TextBox();
        TDLVEE.setName("TDLVEE"); // NOI18N
        TDLVEE.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TDLVEEKeyPressed(evt);
            }
        });
        FormInput.add(TDLVEE);
        TDLVEE.setBounds(585, 480, 100, 23);

        // 9. E'/A'
        jLabelTDLVEA = new widget.Label();
        jLabelTDLVEA.setText("E'/A' :");
        jLabelTDLVEA.setName("jLabelTDLVEA"); // NOI18N
        FormInput.add(jLabelTDLVEA);
        jLabelTDLVEA.setBounds(400, 510, 180, 23);

        TDLVEA = new widget.TextBox();
        TDLVEA.setName("TDLVEA"); // NOI18N
        TDLVEA.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TDLVEAKeyPressed(evt);
            }
        });
        FormInput.add(TDLVEA);
        TDLVEA.setBounds(585, 510, 100, 23);

        // Sub-header: Inferior Cava Vein
        jLabelInferiorCavaVein = new widget.Label();
        jLabelInferiorCavaVein.setText("Inferior Cava Vein");
        jLabelInferiorCavaVein.setName("jLabelInferiorCavaVein"); // NOI18N
        jLabelInferiorCavaVein.setFont(new java.awt.Font("Tahoma", 1, 11)); // Bold
        FormInput.add(jLabelInferiorCavaVein);
        jLabelInferiorCavaVein.setBounds(400, 540, 180, 23);

        // 10. Diameter
        jLabelIVCDiameter = new widget.Label();
        jLabelIVCDiameter.setText("Diameter (mm) :");
        jLabelIVCDiameter.setName("jLabelIVCDiameter"); // NOI18N
        FormInput.add(jLabelIVCDiameter);
        jLabelIVCDiameter.setBounds(400, 565, 180, 23);

        IVCDiameter = new widget.TextBox();
        IVCDiameter.setName("IVCDiameter"); // NOI18N
        IVCDiameter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                IVCDiameterKeyPressed(evt);
            }
        });
        FormInput.add(IVCDiameter);
        IVCDiameter.setBounds(585, 565, 100, 23);

        // 11. RaP
        jLabelIVCRaP = new widget.Label();
        jLabelIVCRaP.setText("RaP :");
        jLabelIVCRaP.setName("jLabelIVCRaP"); // NOI18N
        FormInput.add(jLabelIVCRaP);
        jLabelIVCRaP.setBounds(400, 595, 180, 23);

        IVCRaP = new widget.TextBox();
        IVCRaP.setName("IVCRaP"); // NOI18N
        IVCRaP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                IVCRaPKeyPressed(evt);
            }
        });
        FormInput.add(IVCRaP);
        IVCRaP.setBounds(585, 595, 100, 23);

        // CUSTOM HASIL PEMERIKSAAN ECHO - SPECTRAL DOPPLER - Separator (penutup)
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator4.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator4.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator4.setName("jSeparator4"); // NOI18N
        FormInput.add(jSeparator4);
        jSeparator4.setBounds(0, 625, 750, 1);
        // END CUSTOM HASIL PEMERIKSAAN ECHO - SPECTRAL DOPPLER

        // CUSTOM HASIL PEMERIKSAAN ECHO - DESCRIPTION - Header
        jLabelDescription = new widget.Label();
        jLabelDescription.setText("DESCRIPTION");
        jLabelDescription.setName("jLabelDescription"); // NOI18N
        jLabelDescription.setFont(new java.awt.Font("Tahoma", 1, 11)); // Bold
        FormInput.add(jLabelDescription);
        jLabelDescription.setBounds(0, 630, 750, 23);
        // END CUSTOM HASIL PEMERIKSAAN ECHO - DESCRIPTION

        scrollInput.setViewportView(FormInput);

        internalFrame2.add(scrollInput, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Input Hasil Pemeriksaan ECHO", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3"); // NOI18N
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);
        tbObat.setName("tbObat"); // NOI18N
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObatMouseClicked(evt);
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbObat);

        internalFrame3.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tanggal :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08-04-2026" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "08-04-2026" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(205, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('3');
        BtnCari.setToolTipText("Alt+3");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariActionPerformed(evt);
            }
        });
        BtnCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariKeyPressed(evt);
            }
        });
        panelGlass9.add(BtnCari);

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass9.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(LCount);

        internalFrame3.add(panelGlass9, java.awt.BorderLayout.PAGE_END);

        PanelAccor.setBackground(new java.awt.Color(255, 255, 255));
        PanelAccor.setName("PanelAccor"); // NOI18N
        PanelAccor.setPreferredSize(new java.awt.Dimension(430, 43));
        PanelAccor.setLayout(new java.awt.BorderLayout(1, 1));

        ChkAccor.setBackground(new java.awt.Color(255, 250, 248));
        ChkAccor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png"))); // NOI18N
        ChkAccor.setSelected(true);
        ChkAccor.setFocusable(false);
        ChkAccor.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkAccor.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkAccor.setName("ChkAccor"); // NOI18N
        ChkAccor.setPreferredSize(new java.awt.Dimension(15, 20));
        ChkAccor.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kiri.png"))); // NOI18N
        ChkAccor.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kanan.png"))); // NOI18N
        ChkAccor.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/kanan.png"))); // NOI18N
        ChkAccor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkAccorActionPerformed(evt);
            }
        });
        PanelAccor.add(ChkAccor, java.awt.BorderLayout.WEST);

        TabData.setBackground(new java.awt.Color(254, 255, 254));
        TabData.setForeground(new java.awt.Color(50, 50, 50));
        TabData.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TabData.setName("TabData"); // NOI18N
        TabData.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabDataMouseClicked(evt);
            }
        });

        FormPhoto.setBackground(new java.awt.Color(255, 255, 255));
        FormPhoto.setBorder(null);
        FormPhoto.setName("FormPhoto"); // NOI18N
        FormPhoto.setPreferredSize(new java.awt.Dimension(115, 73));
        FormPhoto.setLayout(new java.awt.BorderLayout());

        FormPass3.setBackground(new java.awt.Color(255, 255, 255));
        FormPass3.setBorder(null);
        FormPass3.setName("FormPass3"); // NOI18N
        FormPass3.setPreferredSize(new java.awt.Dimension(115, 40));

        btnAmbil.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        btnAmbil.setMnemonic('U');
        btnAmbil.setText("Ambil");
        btnAmbil.setToolTipText("Alt+U");
        btnAmbil.setName("btnAmbil"); // NOI18N
        btnAmbil.setPreferredSize(new java.awt.Dimension(100, 30));
        btnAmbil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAmbilActionPerformed(evt);
            }
        });
        FormPass3.add(btnAmbil);

        BtnRefreshPhoto1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/refresh.png"))); // NOI18N
        BtnRefreshPhoto1.setMnemonic('U');
        BtnRefreshPhoto1.setText("Refresh");
        BtnRefreshPhoto1.setToolTipText("Alt+U");
        BtnRefreshPhoto1.setName("BtnRefreshPhoto1"); // NOI18N
        BtnRefreshPhoto1.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnRefreshPhoto1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnRefreshPhoto1ActionPerformed(evt);
            }
        });
        FormPass3.add(BtnRefreshPhoto1);

        FormPhoto.add(FormPass3, java.awt.BorderLayout.PAGE_END);

        Scroll5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll5.setName("Scroll5"); // NOI18N
        Scroll5.setOpaque(true);
        Scroll5.setPreferredSize(new java.awt.Dimension(200, 200));

        LoadHTML2.setBorder(null);
        LoadHTML2.setName("LoadHTML2"); // NOI18N
        Scroll5.setViewportView(LoadHTML2);

        FormPhoto.add(Scroll5, java.awt.BorderLayout.CENTER);

        TabData.addTab("Gambar Pemeriksaan ECHO", FormPhoto);

        FormOrthan.setBackground(new java.awt.Color(255, 255, 255));
        FormOrthan.setBorder(null);
        FormOrthan.setName("FormOrthan"); // NOI18N
        FormOrthan.setPreferredSize(new java.awt.Dimension(115, 73));
        FormOrthan.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255)));
        Scroll6.setName("Scroll6"); // NOI18N
        Scroll6.setOpaque(true);

        tbListDicom.setName("tbListDicom"); // NOI18N
        Scroll6.setViewportView(tbListDicom);

        FormOrthan.add(Scroll6, java.awt.BorderLayout.CENTER);

        panelGlass7.setBorder(null);
        panelGlass7.setName("panelGlass7"); // NOI18N
        panelGlass7.setPreferredSize(new java.awt.Dimension(115, 40));

        btnDicom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/item.png"))); // NOI18N
        btnDicom.setMnemonic('T');
        btnDicom.setText("Tampilkan DICOM");
        btnDicom.setToolTipText("Alt+T");
        btnDicom.setName("btnDicom"); // NOI18N
        btnDicom.setPreferredSize(new java.awt.Dimension(150, 30));
        btnDicom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDicomActionPerformed(evt);
            }
        });
        panelGlass7.add(btnDicom);

        btnUploud.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/2276087_document_extension_format_jpg_paper_icon.png"))); // NOI18N
        btnUploud.setMnemonic('T');
        btnUploud.setText("Uploud Photo");
        btnUploud.setToolTipText("Alt+T");
        btnUploud.setName("btnUploud"); // NOI18N
        btnUploud.setPreferredSize(new java.awt.Dimension(127, 30));
        btnUploud.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUploudActionPerformed(evt);
            }
        });
        panelGlass7.add(btnUploud);

        FormOrthan.add(panelGlass7, java.awt.BorderLayout.PAGE_END);

        TabData.addTab("Integrasi Orthanc", FormOrthan);

        PanelAccor.add(TabData, java.awt.BorderLayout.CENTER);

        internalFrame3.add(PanelAccor, java.awt.BorderLayout.EAST);

        TabRawat.addTab("Data Hasil Pemeriksaan ECHO", internalFrame3);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);
        TabRawat.getAccessibleContext().setAccessibleName("Input Hasil Pemeriksaan ECHOCARDIOGRAFI");

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            isRawat();
        }else{            
            Valid.pindah(evt,TCari,BtnDokter);
        }
}//GEN-LAST:event_TNoRwKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(TNoRM.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Nama Pasien");
        }else if(NmDokter.getText().trim().equals("")){
            Valid.textKosong(BtnDokter,"Dokter");
        }else if(Sistolik.getText().trim().equals("")){
            Valid.textKosong(Sistolik,"Sitolik");
        }else if(Kesimpulan.getText().trim().equals("")){
            Valid.textKosong(Kesimpulan,"Kesimpulan");
        }else{
            if(akses.getkode().equals("Admin Utama")){
                simpan();
            }else{
                if(TanggalRegistrasi.getText().equals("")){
                    TanggalRegistrasi.setText(Sequel.cariIsi("select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));
                }
                if(Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Tanggal.getSelectedItem().toString().substring(11,19))==true){
                    simpan();
                }
            }
        }
    
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{
            Valid.pindah(evt,Kesimpulan,BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
}//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            emptTeks();
        }else{Valid.pindah(evt, BtnSimpan, BtnHapus);}
}//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if(tbObat.getSelectedRow()>-1){
            if(akses.getkode().equals("Admin Utama")){
                hapus();
            }else{
                if(KdDokter.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString())){
                    if(Sequel.cekTanggal48jam(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString(),Sequel.ambiltanggalsekarang())==true){
                        hapus();
                    }
                }else{
                    JOptionPane.showMessageDialog(null,"Hanya bisa dihapus oleh dokter yang bersangkutan..!!");
                }
            }
        }else{
            JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
        } 
}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnHapusActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnBatal, BtnEdit);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        if(TNoRM.getText().trim().equals("")){
            Valid.textKosong(TNoRw,"Nama Pasien");
        }else if(NmDokter.getText().trim().equals("")){
            Valid.textKosong(BtnDokter,"Dokter");
        }else if(Sistolik.getText().trim().equals("")){
            Valid.textKosong(Sistolik,"Sistolik");
        }else if(Kesimpulan.getText().trim().equals("")){
            Valid.textKosong(Kesimpulan,"Kesimpulan");
        }else{
            if(tbObat.getSelectedRow()>-1){
                if(akses.getkode().equals("Admin Utama")){
                    ganti();
                }else{
                    if(KdDokter.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(),4).toString())){
                        if(Sequel.cekTanggal48jam(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString(),Sequel.ambiltanggalsekarang())==true){
                            if(TanggalRegistrasi.getText().equals("")){
                                TanggalRegistrasi.setText(Sequel.cariIsi("select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText()));
                            }
                            if(Sequel.cekTanggalRegistrasi(TanggalRegistrasi.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Tanggal.getSelectedItem().toString().substring(11,19))==true){
                                ganti();
                            }
                        }
                    }else{
                        JOptionPane.showMessageDialog(null,"Hanya bisa diganti oleh dokter yang bersangkutan..!!");
                    }
                }
            }else{
                JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih data terlebih dahulu..!!");
            }
        }
}//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnEditActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnHapus, BtnPrint);
        }
}//GEN-LAST:event_BtnEditKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnKeluarActionPerformed(null);
        }else{Valid.pindah(evt,BtnEdit,TCari);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            BtnBatal.requestFocus();
        }else if(tabMode.getRowCount()!=0){
            try{
                htmlContent = new StringBuilder();
                htmlContent.append(                             
                    "<tr class='isi'>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>No.Rawat</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>No.RM</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Nama Pasien</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Tgl.Lahir</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Kode Dokter</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Nama Dokter</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Tanggal</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Fungsi Sistolik LV</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Fungsi Diastolik LV</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Kontraktilitas RV</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Dimensi Ruang Jantung</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Katup-katup</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Analisa Segmental</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>eRAP</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Lain-lain</b></td>"+
                        "<td valign='middle' bgcolor='#FFFAF8' align='center'><b>Kesimpulan</b></td>"+
                    "</tr>"
                );
                for (i = 0; i < tabMode.getRowCount(); i++) {
                    htmlContent.append(
                        "<tr class='isi'>"+
                           "<td valign='top'>"+tbObat.getValueAt(i,0).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,1).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,2).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,3).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,4).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,5).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,6).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,7).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,8).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,9).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,10).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,11).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,12).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,13).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,14).toString()+"</td>"+
                            "<td valign='top'>"+tbObat.getValueAt(i,15).toString()+"</td>"+
                        "</tr>");
                }
                LoadHTML.setText(
                    "<html>"+
                      "<table width='2000px' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>"+
                       htmlContent.toString()+
                      "</table>"+
                    "</html>"
                );

                File g = new File("file2.css");            
                BufferedWriter bg = new BufferedWriter(new FileWriter(g));
                bg.write(
                    ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                    ".isi2 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#323232;}"+
                    ".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                    ".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"+
                    ".isi5 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#AA0000;}"+
                    ".isi6 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#FF0000;}"+
                    ".isi7 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#C8C800;}"+
                    ".isi8 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#00AA00;}"+
                    ".isi9 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#969696;}"
                );
                bg.close();

                File f = new File("DataHasilPemeriksaanECHO.html");            
                BufferedWriter bw = new BufferedWriter(new FileWriter(f));            
                bw.write(LoadHTML.getText().replaceAll("<head>","<head>"+
                            "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />"+
                            "<table width='2000px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"+
                                "<tr class='isi2'>"+
                                    "<td valign='top' align='center'>"+
                                        "<font size='4' face='Tahoma'>"+akses.getnamars()+"</font><br>"+
                                        akses.getalamatrs()+", "+akses.getkabupatenrs()+", "+akses.getpropinsirs()+"<br>"+
                                        akses.getkontakrs()+", E-mail : "+akses.getemailrs()+"<br><br>"+
                                        "<font size='2' face='Tahoma'>DATA HASIL PEMERIKSAAN ECHO<br><br></font>"+        
                                    "</td>"+
                               "</tr>"+
                            "</table>")
                );
                bw.close();                         
                Desktop.getDesktop().browse(f.toURI());

            }catch(Exception e){
                System.out.println("Notifikasi : "+e);
            }
        }
        this.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnEdit, BtnKeluar);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }
}//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        runBackground(() ->tampil());
}//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt, TCari, BtnAll);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        runBackground(() ->tampil());
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            TCari.setText("");
            runBackground(() ->tampil());
        }else{
            Valid.pindah(evt, BtnCari, TPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                isPhoto();
                panggilPhoto();
                getData();
                tampilOrthanc();
            } catch (java.lang.NullPointerException e) {
            }
            if((evt.getClickCount()==2)&&(tbObat.getSelectedColumn()==0)){
                TabRawat.setSelectedIndex(0);
            }
        }
}//GEN-LAST:event_tbObatMouseClicked

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyPressed
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }else if(evt.getKeyCode()==KeyEvent.VK_SPACE){
                try {
                    getData();
                    TabRawat.setSelectedIndex(0);
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
}//GEN-LAST:event_tbObatKeyPressed

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
        if (dokter == null || !dokter.isDisplayable()) {
            dokter=new DlgCariDokter(null,false);
            dokter.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dokter.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if(dokter.getTable().getSelectedRow()!= -1){        
                         KdDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),0).toString());
                         NmDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(),1).toString());
                    }  
                    BtnDokter.requestFocus();
                    dokter=null;
                }
            });
            dokter.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
            dokter.setLocationRelativeTo(internalFrame1);
        }   
        if (dokter == null) return;
        dokter.isCek();
        if (dokter.isVisible()) {
            dokter.toFront();
            return;
        }
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnDokterActionPerformed

    private void BtnDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnDokterKeyPressed
        Valid.pindah(evt,TCari,Tanggal);
    }//GEN-LAST:event_BtnDokterKeyPressed

    private void TanggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalKeyPressed
        Valid.pindah2(evt,BtnDokter,Sistolik);
    }//GEN-LAST:event_TanggalKeyPressed

    private void MnPenilaianMedisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnPenilaianMedisActionPerformed
        if(tbObat.getSelectedRow()>-1){
            Map<String, Object> param = new HashMap<>();
            param.put("namars",akses.getnamars());
            param.put("alamatrs",akses.getalamatrs());
            param.put("kotars",akses.getkabupatenrs());
            param.put("propinsirs",akses.getpropinsirs());
            param.put("kontakrs",akses.getkontakrs());
            param.put("emailrs",akses.getemailrs());          
            param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
            finger=Sequel.cariIsi("select sha1(sidikjari.sidikjari) from sidikjari inner join pegawai on pegawai.id=sidikjari.id where pegawai.nik=?",tbObat.getValueAt(tbObat.getSelectedRow(),4).toString());
            param.put("finger","Dikeluarkan di "+akses.getnamars()+", Kabupaten/Kota "+akses.getkabupatenrs()+"\nDitandatangani secara elektronik oleh "+tbObat.getValueAt(tbObat.getSelectedRow(),5).toString()+"\nID "+(finger.equals("")?tbObat.getValueAt(tbObat.getSelectedRow(),4).toString():finger)+"\n"+Valid.SetTgl3(tbObat.getValueAt(tbObat.getSelectedRow(),6).toString())); 
            param.put("hasil","http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/hasilpemeriksaanecho/"+Sequel.cariIsi("select hasil_pemeriksaan_echo_gambar.photo from hasil_pemeriksaan_echo_gambar where hasil_pemeriksaan_echo_gambar.no_rawat=?",tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()));

            // ============================================================================
            // CUSTOM REPORT ECHO - 25 Juni 2026
            // ============================================================================
            // TOTAL: 41 kolom (12 kolom original + 29 kolom custom)
            //
            // KOLOM ORIGINAL (12):
            // - no_rawat, no_rkm_medis, nm_pasien, tgl_lahir, tanggal
            // - kd_dokter, nm_dokter
            // - sistolik, diastolic, kontraktilitas, dimensi_ruang
            // - katup, analisa_segmental, erap, lain_lain, kesimpulan
            //
            // KOLOM CUSTOM - HISTORY/CLINICAL DIAGNOSIS (18):
            // - aorta, left_atrium, rv_diastolic, ejection_fraction
            // - epss, lvot_diameter, lv_edd, lv_esd
            // - ivs_diastolic, lvpw_diastolic, tapse, rvot
            // - aortic_sinus, asc_aorta, aortic_arcus, desc_aorta
            // - abdominal_aorta, rvot_diameter
            //
            // KOLOM CUSTOM - SPECTRAL DOPPLER (11):
            // - dec_time, ea_ratio, tvg, m_pap
            // - emax, ao_vmax, ao_vti
            // - td_lv_ee, td_lv_ea
            // - ivc_diameter, ivc_rap
            //
            // REFERENSI SQL:
            // - custom_penambahan_history_clinical_diagnosis.sql (18 kolom)
            // - custom_penambahan_spectral_doppler.sql (11 kolom)
            //
            // ⚠️ PENTING - JIKA CUSTOM LAGI:
            // Query ini HARUS SINKRON dengan query di file:
            // - report/rptCetakHasilPemeriksaanECHO.jrxml (queryString)
            // Jika menambah/mengurangi kolom di report, UPDATE JUGA QUERY DI SINI!
            // Jika tidak sinkron akan error: "Unknown column name ... in result set"
            // ============================================================================

            Valid.MyReportqry("rptCetakHasilPemeriksaanECHO.jasper","report","::[ Formulir Hasil Pemeriksaan ECHOCARDIOGRAFI ]::",
                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,hasil_pemeriksaan_echo.tanggal,"+
                            "hasil_pemeriksaan_echo.kd_dokter,dokter.nm_dokter,"+
                            // Original fields (12 kolom)
                            "hasil_pemeriksaan_echo.sistolik,hasil_pemeriksaan_echo.diastolic,hasil_pemeriksaan_echo.kontraktilitas,hasil_pemeriksaan_echo.dimensi_ruang,"+
                            "hasil_pemeriksaan_echo.katup,hasil_pemeriksaan_echo.analisa_segmental,hasil_pemeriksaan_echo.erap,hasil_pemeriksaan_echo.lain_lain,"+
                            "hasil_pemeriksaan_echo.kesimpulan,"+
                            // CUSTOM: HISTORY/CLINICAL DIAGNOSIS (18 kolom) - 25 Juni 2026
                            "hasil_pemeriksaan_echo.aorta,hasil_pemeriksaan_echo.left_atrium,hasil_pemeriksaan_echo.rv_diastolic,hasil_pemeriksaan_echo.ejection_fraction,"+
                            "hasil_pemeriksaan_echo.epss,hasil_pemeriksaan_echo.lvot_diameter,hasil_pemeriksaan_echo.lv_edd,hasil_pemeriksaan_echo.lv_esd,"+
                            "hasil_pemeriksaan_echo.ivs_diastolic,hasil_pemeriksaan_echo.lvpw_diastolic,hasil_pemeriksaan_echo.tapse,hasil_pemeriksaan_echo.rvot,"+
                            "hasil_pemeriksaan_echo.aortic_sinus,hasil_pemeriksaan_echo.asc_aorta,hasil_pemeriksaan_echo.aortic_arcus,hasil_pemeriksaan_echo.desc_aorta,"+
                            "hasil_pemeriksaan_echo.abdominal_aorta,hasil_pemeriksaan_echo.rvot_diameter,"+
                            // CUSTOM: SPECTRAL DOPPLER (11 kolom) - 25 Juni 2026
                            "hasil_pemeriksaan_echo.dec_time,hasil_pemeriksaan_echo.ea_ratio,hasil_pemeriksaan_echo.tvg,hasil_pemeriksaan_echo.m_pap,"+
                            "hasil_pemeriksaan_echo.emax,hasil_pemeriksaan_echo.ao_vmax,hasil_pemeriksaan_echo.ao_vti,hasil_pemeriksaan_echo.td_lv_ee,"+
                            "hasil_pemeriksaan_echo.td_lv_ea,hasil_pemeriksaan_echo.ivc_diameter,hasil_pemeriksaan_echo.ivc_rap "+
                            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                            "inner join hasil_pemeriksaan_echo on reg_periksa.no_rawat=hasil_pemeriksaan_echo.no_rawat "+
                            "inner join dokter on hasil_pemeriksaan_echo.kd_dokter=dokter.kd_dokter where hasil_pemeriksaan_echo.no_rawat='"+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"'",param);
        }
    }//GEN-LAST:event_MnPenilaianMedisActionPerformed

    private void SistolikKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SistolikKeyPressed
        Valid.pindah(evt,Tanggal,Diastolik);
    }//GEN-LAST:event_SistolikKeyPressed

    private void KatupKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KatupKeyPressed
        Valid.pindah(evt,Dimensi,AnalisaSegmental);
    }//GEN-LAST:event_KatupKeyPressed

    private void DiastolikKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DiastolikKeyPressed
        Valid.pindah(evt,Sistolik,Kontraktilitas);
    }//GEN-LAST:event_DiastolikKeyPressed

    private void AnalisaSegmentalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AnalisaSegmentalKeyPressed
        Valid.pindah(evt,Katup,Erap);
    }//GEN-LAST:event_AnalisaSegmentalKeyPressed

    private void DimensiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DimensiKeyPressed
        Valid.pindah(evt,Kontraktilitas,Katup);
    }//GEN-LAST:event_DimensiKeyPressed

    private void KesimpulanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KesimpulanKeyPressed
        Valid.pindah2(evt,LainLain,BtnSimpan);
    }//GEN-LAST:event_KesimpulanKeyPressed

    private void ChkAccorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkAccorActionPerformed
        if(tbObat.getSelectedRow()!= -1){
            isPhoto();
            panggilPhoto();
        }else{
            ChkAccor.setSelected(false);
            JOptionPane.showMessageDialog(null,"Silahkan pilih No.Pernyataan..!!!");
        }
    }//GEN-LAST:event_ChkAccorActionPerformed

    private void ErapKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ErapKeyPressed
        Valid.pindah(evt,AnalisaSegmental,LainLain);
    }//GEN-LAST:event_ErapKeyPressed

    private void KontraktilitasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KontraktilitasKeyPressed
        Valid.pindah(evt,Diastolik,Dimensi);
    }//GEN-LAST:event_KontraktilitasKeyPressed

    private void LainLainKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LainLainKeyPressed
        Valid.pindah(evt,Erap,Aorta);
    }//GEN-LAST:event_LainLainKeyPressed

    // CUSTOM HASIL PEMERIKSAAN ECHO - HISTORY/CLINICAL DIAGNOSIS - KeyPressed Event Handlers
    private void AortaKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,LainLain,LeftAtrium);
    }

    private void LeftAtriumKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,Aorta,RVDiastolic);
    }

    private void RVDiastolicKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,LeftAtrium,EjectionFraction);
    }

    private void EjectionFractionKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RVDiastolic,EPSS);
    }

    private void EPSSKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,EjectionFraction,LVOTDiameter);
    }

    private void LVOTDiameterKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,EPSS,LVEDD);
    }

    private void LVEDDKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,LVOTDiameter,LVESD);
    }

    private void LVESDKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,LVEDD,IVSDiastolic);
    }

    private void IVSDiastolicKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,LVESD,LVPWDiastolic);
    }

    private void LVPWDiastolicKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,IVSDiastolic,TAPSE);
    }

    private void TAPSEKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,LVPWDiastolic,RVOT);
    }

    private void RVOTKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,TAPSE,AorticSinus);
    }

    private void AorticSinusKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RVOT,AscAorta);
    }

    private void AscAortaKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,AorticSinus,AorticArcus);
    }

    private void AorticArcusKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,AscAorta,DescAorta);
    }

    private void DescAortaKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,AorticArcus,AbdominalAorta);
    }

    private void AbdominalAortaKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,DescAorta,RVOTDiameter);
    }

    private void RVOTDiameterKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,AbdominalAorta,DecTime);
    }
    // END CUSTOM HASIL PEMERIKSAAN ECHO - HISTORY/CLINICAL DIAGNOSIS - KeyPressed Event Handlers

    // CUSTOM HASIL PEMERIKSAAN ECHO - SPECTRAL DOPPLER - KeyPressed Event Handlers
    private void DecTimeKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,RVOTDiameter,EARatio);
    }

    private void EARatioKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,DecTime,TVG);
    }

    private void TVGKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,EARatio,MPAP);
    }

    private void MPAPKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,TVG,Emax);
    }

    private void EmaxKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,MPAP,AoVmax);
    }

    private void AoVmaxKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,Emax,AoVTI);
    }

    private void AoVTIKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,AoVmax,TDLVEE);
    }

    private void TDLVEEKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,AoVTI,TDLVEA);
    }

    private void TDLVEAKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,TDLVEE,IVCDiameter);
    }

    private void IVCDiameterKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,TDLVEA,IVCRaP);
    }

    private void IVCRaPKeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt,IVCDiameter,Kesimpulan);
    }
    // END CUSTOM HASIL PEMERIKSAAN ECHO - SPECTRAL DOPPLER - KeyPressed Event Handlers

    private void btnAmbilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAmbilActionPerformed
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else{
            if(tbObat.getSelectedRow()>-1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Valid.panggilUrl("hasilpemeriksaanecho/login.php?act=login&usere="+koneksiDB.USERHYBRIDWEB()+"&passwordte="+koneksiDB.PASHYBRIDWEB()+"&no_rawat="+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih No.Rawat terlebih dahulu..!!");
            }
        }
    }//GEN-LAST:event_btnAmbilActionPerformed

    private void BtnRefreshPhoto1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnRefreshPhoto1ActionPerformed
        if(tbObat.getSelectedRow()>-1){
            panggilPhoto();
        }else{
            JOptionPane.showMessageDialog(rootPane,"Silahkan anda pilih No.Rawat terlebih dahulu..!!");
        }
    }//GEN-LAST:event_BtnRefreshPhoto1ActionPerformed

    private void btnDicomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDicomActionPerformed
        if(tabModeDicom.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else {
            if(tbListDicom.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                OrthancDICOM orthan=new OrthancDICOM(null,false);
                orthan.setJudul("::[ DICOM Orthanc Pasien "+tbObat.getValueAt(tbObat.getSelectedRow(),1).toString()+" "+tbObat.getValueAt(tbObat.getSelectedRow(),2).toString()+", Series "+tbListDicom.getValueAt(tbListDicom.getSelectedRow(),2).toString()+" ]::",tbObat.getValueAt(tbObat.getSelectedRow(),0).toString().replaceAll("/","")+"_"+tbObat.getValueAt(tbObat.getSelectedRow(),1).toString()+"_"+tbObat.getValueAt(tbObat.getSelectedRow(),2).toString().replaceAll(" ","_").replaceAll("/",""),tbListDicom.getValueAt(tbListDicom.getSelectedRow(),2).toString(),tbListDicom.getValueAt(tbListDicom.getSelectedRow(),1).toString(),tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                try {
                    orthan.loadURL(koneksiDB.URLORTHANC()+":"+koneksiDB.PORTORTHANC()+"/web-viewer/app/viewer.html?series="+tbListDicom.getValueAt(tbListDicom.getSelectedRow(),2).toString());
                } catch (Exception ex) {
                    System.out.println("Notifikasi : "+ex);
                }
                orthan.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
                orthan.setLocationRelativeTo(internalFrame1);
                orthan.setVisible(true);
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data..!!");
            }
        }
    }//GEN-LAST:event_btnDicomActionPerformed

    private void TabDataMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabDataMouseClicked
        tampilOrthanc();
    }//GEN-LAST:event_TabDataMouseClicked

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
            });
        }
    }//GEN-LAST:event_formWindowOpened

    private void btnUploudActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUploudActionPerformed
        if(tabModeDicom.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis...!!!!");
            TCari.requestFocus();
        }else {
            if(tbListDicom.getSelectedRow()!= -1){
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Sequel.queryu2("delete from hasil_pemeriksaan_echo_gambar where no_rawat=?",1,new String[]{tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()});
                ApiOrthanc orthanc=new ApiOrthanc();
                orthanc.AmbilJpg2(tbListDicom.getValueAt(tbListDicom.getSelectedRow(),2).toString());
                try {
                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    HttpPost post = new HttpPost("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/hasilpemeriksaanecho/pages/upload/service.php");
                    System.out.println("http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/hasilpemeriksaanecho/pages/upload/service.php");
                    post.setHeader("Content-Type", "application/json");
                    post.addHeader("username", koneksiDB.USERHYBRIDWEB());
                    post.addHeader("password", koneksiDB.PASHYBRIDWEB());
                    File f = new File("./gambarradiologi/"+tbListDicom.getValueAt(tbListDicom.getSelectedRow(),2).toString()+".jpg");
                    byte[] fileContent = Files.readAllBytes(f.toPath());
                    String json="{" +
                                    "\"file\":\""+Base64.getEncoder().encodeToString(fileContent)+"\"," +
                                    "\"namafile\":\""+tbListDicom.getValueAt(tbListDicom.getSelectedRow(),2).toString()+".jpg\"," +
                                    "\"norawat\":\""+tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()+"\"" +
                                "}";
                    post.setEntity(new StringEntity(json));
                    try (CloseableHttpResponse response = httpClient.execute(post)) {
                        json=EntityUtils.toString(response.getEntity());
                        root = mapper.readTree(json);
                        JOptionPane.showMessageDialog(null,root.path("metadata").path("message").asText());
                    } catch (IOException a) {
                        System.out.println("Notifikasi : " + a);
                        JOptionPane.showMessageDialog(null,""+a);
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi : " + e);
                    JOptionPane.showMessageDialog(null,""+e);
                }
                this.setCursor(Cursor.getDefaultCursor());
            }else{
                JOptionPane.showMessageDialog(null,"Maaf, Silahkan pilih data..!!");
            }
        }
    }//GEN-LAST:event_btnUploudActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMHasilPemeriksaanEcho dialog = new RMHasilPemeriksaanEcho(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // CUSTOM HASIL PEMERIKSAAN ECHO - HISTORY/CLINICAL DIAGNOSIS - 2D/M Mode 31 - Variable Declaration
    private widget.TextBox AbdominalAorta;
    private widget.TextBox AnalisaSegmental;
    private widget.TextBox Aorta;
    private widget.TextBox AorticArcus;
    private widget.TextBox AorticSinus;
    private widget.TextBox AscAorta;
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnDokter;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnRefreshPhoto1;
    private widget.Button BtnSimpan;
    private widget.CekBox ChkAccor;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.TextBox DescAorta;
    private widget.TextBox Diastolik;
    private widget.TextBox Dimensi;
    private widget.TextBox EPSS;
    private widget.TextBox EjectionFraction;
    private widget.TextBox Erap;
    private widget.PanelBiasa FormInput;
    private widget.PanelBiasa FormOrthan;
    private widget.PanelBiasa FormPass3;
    private widget.PanelBiasa FormPhoto;
    private widget.TextBox Katup;
    private widget.TextBox KdDokter;
    private widget.TextArea Kesimpulan;
    private widget.TextBox IVSDiastolic;
    private widget.TextBox Kontraktilitas;
    private widget.Label LCount;
    private widget.TextBox LVEDD;
    private widget.TextBox LVESD;
    private widget.TextBox LVOTDiameter;
    private widget.TextBox LVPWDiastolic;
    private widget.TextBox LainLain;
    private widget.TextBox LeftAtrium;
    private widget.editorpane LoadHTML;
    private widget.editorpane LoadHTML2;
    private widget.MenuItem MnPenilaianMedis;
    private widget.TextBox NmDokter;
    private widget.PanelBiasa PanelAccor;
    private widget.TextBox RVOT;
    private widget.TextBox RVDiastolic;
    private widget.TextBox RVOTDiameter;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll5;
    private widget.ScrollPane Scroll6;
    private widget.TextBox Sistolik;
    private widget.TextBox TAPSE;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.TabPane TabData;
    private widget.TabPane TabRawat;
    private widget.Tanggal Tanggal;
    private widget.TextBox TanggalRegistrasi;
    private widget.TextBox TglLahir;
    private widget.Button btnAmbil;
    private widget.Button btnDicom;
    private widget.Button btnUploud;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.Label jLabel10;
    private widget.Label jLabel19;
    private widget.Label jLabel2DMode;
    private widget.Label jLabelAbdominalAorta;
    private widget.Label jLabelAorta;
    private widget.Label jLabelAorticArcus;
    private widget.Label jLabelAorticSinus;
    private widget.Label jLabelAscAorta;
    private widget.Label jLabelDescAorta;
    private widget.Label jLabelEPSS;
    private widget.Label jLabelEjectionFraction;
    private widget.Label jLabelHistoryClinical;
    private widget.Label jLabelIVSDiastolic;
    private widget.Label jLabelLVEDD;
    private widget.Label jLabelLVESD;
    private widget.Label jLabelLVOTDiameter;
    private widget.Label jLabelLVPWDiastolic;
    private widget.Label jLabelLeftAtrium;
    private widget.Label jLabelRVDiastolic;
    private widget.Label jLabelRVOT;
    private widget.Label jLabelRVOTDiameter;
    private widget.Label jLabelTAPSE;
    private widget.Label jLabel21;
    private widget.Label jLabel31;
    private widget.Label jLabel35;
    private widget.Label jLabel40;
    private widget.Label jLabel41;
    private widget.Label jLabel42;
    private widget.Label jLabel43;
    private widget.Label jLabel44;
    private widget.Label jLabel45;
    private widget.Label jLabel46;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.PopupMenu jPopupMenu1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private widget.Label jLabelSpectralDoppler;
    private widget.Label jLabelDescription;
    // CUSTOM HASIL PEMERIKSAAN ECHO - SPECTRAL DOPPLER - Variable Declaration
    private widget.TextBox DecTime;
    private widget.TextBox EARatio;
    private widget.TextBox TVG;
    private widget.TextBox MPAP;
    private widget.TextBox Emax;
    private widget.TextBox AoVmax;
    private widget.TextBox AoVTI;
    private widget.TextBox TDLVEE;
    private widget.TextBox TDLVEA;
    private widget.TextBox IVCDiameter;
    private widget.TextBox IVCRaP;
    private widget.Label jLabelDecTime;
    private widget.Label jLabelEARatio;
    private widget.Label jLabelTVG;
    private widget.Label jLabelMPAP;
    private widget.Label jLabelEmax;
    private widget.Label jLabelAoVmax;
    private widget.Label jLabelAoVTI;
    private widget.Label jLabelTDLVEE;
    private widget.Label jLabelTDLVEA;
    private widget.Label jLabelIVCDiameter;
    private widget.Label jLabelIVCRaP;
    private widget.Label jLabelMitralInflow;
    private widget.Label jLabelTricuspidRegurgitation;
    private widget.Label jLabelPulmonary;
    private widget.Label jLabelTissueDoppler;
    private widget.Label jLabelTissueDopplerLV;
    private widget.Label jLabelInferiorCavaVein;
    // END CUSTOM HASIL PEMERIKSAAN ECHO - SPECTRAL DOPPLER - Variable Declaration
    private widget.Label label11;
    private widget.Label label14;
    private widget.panelisi panelGlass7;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollInput;
    private widget.ScrollPane scrollPane17;
    private widget.Table tbListDicom;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try{
            if(TCari.getText().trim().equals("")){
                // CUSTOM HASIL PEMERIKSAAN ECHO - SELECT dengan 41 field (12 original + 18 history + 11 spectral)
                ps=koneksi.prepareStatement(
                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,hasil_pemeriksaan_echo.tanggal,"+
                            "hasil_pemeriksaan_echo.kd_dokter,dokter.nm_dokter,"+
                            "hasil_pemeriksaan_echo.sistolik,hasil_pemeriksaan_echo.diastolic,hasil_pemeriksaan_echo.kontraktilitas,hasil_pemeriksaan_echo.dimensi_ruang,"+
                            "hasil_pemeriksaan_echo.katup,hasil_pemeriksaan_echo.analisa_segmental,hasil_pemeriksaan_echo.erap,hasil_pemeriksaan_echo.lain_lain,"+
                            "hasil_pemeriksaan_echo.kesimpulan,"+
                            "hasil_pemeriksaan_echo.aorta,hasil_pemeriksaan_echo.left_atrium,hasil_pemeriksaan_echo.rv_diastolic,hasil_pemeriksaan_echo.ejection_fraction,"+
                            "hasil_pemeriksaan_echo.epss,hasil_pemeriksaan_echo.lvot_diameter,hasil_pemeriksaan_echo.lv_edd,hasil_pemeriksaan_echo.lv_esd,"+
                            "hasil_pemeriksaan_echo.ivs_diastolic,hasil_pemeriksaan_echo.lvpw_diastolic,hasil_pemeriksaan_echo.tapse,hasil_pemeriksaan_echo.rvot,"+
                            "hasil_pemeriksaan_echo.aortic_sinus,hasil_pemeriksaan_echo.asc_aorta,hasil_pemeriksaan_echo.aortic_arcus,hasil_pemeriksaan_echo.desc_aorta,"+
                            "hasil_pemeriksaan_echo.abdominal_aorta,hasil_pemeriksaan_echo.rvot_diameter,"+
                            "hasil_pemeriksaan_echo.dec_time,hasil_pemeriksaan_echo.ea_ratio,hasil_pemeriksaan_echo.tvg,hasil_pemeriksaan_echo.m_pap,"+
                            "hasil_pemeriksaan_echo.emax,hasil_pemeriksaan_echo.ao_vmax,hasil_pemeriksaan_echo.ao_vti,hasil_pemeriksaan_echo.td_lv_ee,"+
                            "hasil_pemeriksaan_echo.td_lv_ea,hasil_pemeriksaan_echo.ivc_diameter,hasil_pemeriksaan_echo.ivc_rap "+
                            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                            "inner join hasil_pemeriksaan_echo on reg_periksa.no_rawat=hasil_pemeriksaan_echo.no_rawat "+
                            "inner join dokter on hasil_pemeriksaan_echo.kd_dokter=dokter.kd_dokter where "+
                            "hasil_pemeriksaan_echo.tanggal between ? and ? order by hasil_pemeriksaan_echo.tanggal");
            }else{
                // CUSTOM HASIL PEMERIKSAAN ECHO - SELECT dengan 41 field (12 original + 18 history + 11 spectral) dengan search
                ps=koneksi.prepareStatement(
                            "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,pasien.tgl_lahir,hasil_pemeriksaan_echo.tanggal,"+
                            "hasil_pemeriksaan_echo.kd_dokter,dokter.nm_dokter,"+
                            "hasil_pemeriksaan_echo.sistolik,hasil_pemeriksaan_echo.diastolic,hasil_pemeriksaan_echo.kontraktilitas,hasil_pemeriksaan_echo.dimensi_ruang,"+
                            "hasil_pemeriksaan_echo.katup,hasil_pemeriksaan_echo.analisa_segmental,hasil_pemeriksaan_echo.erap,hasil_pemeriksaan_echo.lain_lain,"+
                            "hasil_pemeriksaan_echo.kesimpulan,"+
                            "hasil_pemeriksaan_echo.aorta,hasil_pemeriksaan_echo.left_atrium,hasil_pemeriksaan_echo.rv_diastolic,hasil_pemeriksaan_echo.ejection_fraction,"+
                            "hasil_pemeriksaan_echo.epss,hasil_pemeriksaan_echo.lvot_diameter,hasil_pemeriksaan_echo.lv_edd,hasil_pemeriksaan_echo.lv_esd,"+
                            "hasil_pemeriksaan_echo.ivs_diastolic,hasil_pemeriksaan_echo.lvpw_diastolic,hasil_pemeriksaan_echo.tapse,hasil_pemeriksaan_echo.rvot,"+
                            "hasil_pemeriksaan_echo.aortic_sinus,hasil_pemeriksaan_echo.asc_aorta,hasil_pemeriksaan_echo.aortic_arcus,hasil_pemeriksaan_echo.desc_aorta,"+
                            "hasil_pemeriksaan_echo.abdominal_aorta,hasil_pemeriksaan_echo.rvot_diameter,"+
                            "hasil_pemeriksaan_echo.dec_time,hasil_pemeriksaan_echo.ea_ratio,hasil_pemeriksaan_echo.tvg,hasil_pemeriksaan_echo.m_pap,"+
                            "hasil_pemeriksaan_echo.emax,hasil_pemeriksaan_echo.ao_vmax,hasil_pemeriksaan_echo.ao_vti,hasil_pemeriksaan_echo.td_lv_ee,"+
                            "hasil_pemeriksaan_echo.td_lv_ea,hasil_pemeriksaan_echo.ivc_diameter,hasil_pemeriksaan_echo.ivc_rap "+
                            "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                            "inner join hasil_pemeriksaan_echo on reg_periksa.no_rawat=hasil_pemeriksaan_echo.no_rawat "+
                            "inner join dokter on hasil_pemeriksaan_echo.kd_dokter=dokter.kd_dokter where "+
                            "hasil_pemeriksaan_echo.tanggal between ? and ? and (reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or "+
                            "hasil_pemeriksaan_echo.kd_dokter like ? or dokter.nm_dokter like ?) order by hasil_pemeriksaan_echo.tanggal");
            }

            try {
                if(TCari.getText().trim().equals("")){
                    ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                    ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                }else{
                    ps.setString(1,Valid.SetTgl(DTPCari1.getSelectedItem()+"")+" 00:00:00");
                    ps.setString(2,Valid.SetTgl(DTPCari2.getSelectedItem()+"")+" 23:59:59");
                    ps.setString(3,"%"+TCari.getText()+"%");
                    ps.setString(4,"%"+TCari.getText()+"%");
                    ps.setString(5,"%"+TCari.getText()+"%");
                    ps.setString(6,"%"+TCari.getText()+"%");
                    ps.setString(7,"%"+TCari.getText()+"%");
                }
                rs=ps.executeQuery();
                while(rs.next()){
                    // CUSTOM HASIL PEMERIKSAAN ECHO - Tambahkan 45 kolom (7 metadata + 9 original + 18 history + 11 spectral)
                    tabMode.addRow(new Object[]{
                        rs.getString("no_rawat"),rs.getString("no_rkm_medis"),rs.getString("nm_pasien"),rs.getDate("tgl_lahir"),rs.getString("kd_dokter"),rs.getString("nm_dokter"),rs.getString("tanggal"),
                        rs.getString("sistolik"),rs.getString("diastolic"),rs.getString("kontraktilitas"),rs.getString("dimensi_ruang"),rs.getString("katup"),rs.getString("analisa_segmental"),
                        rs.getString("erap"),rs.getString("lain_lain"),rs.getString("kesimpulan"),
                        rs.getString("aorta"),rs.getString("left_atrium"),rs.getString("rv_diastolic"),rs.getString("ejection_fraction"),rs.getString("epss"),rs.getString("lvot_diameter"),
                        rs.getString("lv_edd"),rs.getString("lv_esd"),rs.getString("ivs_diastolic"),rs.getString("lvpw_diastolic"),rs.getString("tapse"),rs.getString("rvot"),
                        rs.getString("aortic_sinus"),rs.getString("asc_aorta"),rs.getString("aortic_arcus"),rs.getString("desc_aorta"),rs.getString("abdominal_aorta"),rs.getString("rvot_diameter"),
                        rs.getString("dec_time"),rs.getString("ea_ratio"),rs.getString("tvg"),rs.getString("m_pap"),rs.getString("emax"),rs.getString("ao_vmax"),
                        rs.getString("ao_vti"),rs.getString("td_lv_ee"),rs.getString("td_lv_ea"),rs.getString("ivc_diameter"),rs.getString("ivc_rap")
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
            
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        LCount.setText(""+tabMode.getRowCount());
    }

    public void emptTeks() {
        // CUSTOM HASIL PEMERIKSAAN ECHO - HISTORY/CLINICAL DIAGNOSIS - Clear 18 field baru
        Aorta.setText("");
        LeftAtrium.setText("");
        RVDiastolic.setText("");
        EjectionFraction.setText("");
        EPSS.setText("");
        LVOTDiameter.setText("");
        LVEDD.setText("");
        LVESD.setText("");
        IVSDiastolic.setText("");
        LVPWDiastolic.setText("");
        TAPSE.setText("");
        RVOT.setText("");
        AorticSinus.setText("");
        AscAorta.setText("");
        AorticArcus.setText("");
        DescAorta.setText("");
        AbdominalAorta.setText("");
        RVOTDiameter.setText("");
        // CUSTOM HASIL PEMERIKSAAN ECHO - SPECTRAL DOPPLER - Clear 11 field baru
        DecTime.setText("");
        EARatio.setText("");
        TVG.setText("");
        MPAP.setText("");
        Emax.setText("");
        AoVmax.setText("");
        AoVTI.setText("");
        TDLVEE.setText("");
        TDLVEA.setText("");
        IVCDiameter.setText("");
        IVCRaP.setText("");
        // Field lama yang sudah ada
        Sistolik.setText("");
        Diastolik.setText("");
        Kontraktilitas.setText("");
        Dimensi.setText("");
        Katup.setText("");
        AnalisaSegmental.setText("");
        Erap.setText("");
        LainLain.setText("");
        Kesimpulan.setText("");
        Tanggal.setDate(new Date());
        TabRawat.setSelectedIndex(0);
        Sistolik.requestFocus();
    } 

    private void getData() {
        if(tbObat.getSelectedRow()!= -1){
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(),2).toString());
            TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(),3).toString());
            Sistolik.setText(tbObat.getValueAt(tbObat.getSelectedRow(),7).toString());
            Diastolik.setText(tbObat.getValueAt(tbObat.getSelectedRow(),8).toString());
            Kontraktilitas.setText(tbObat.getValueAt(tbObat.getSelectedRow(),9).toString());
            Dimensi.setText(tbObat.getValueAt(tbObat.getSelectedRow(),10).toString());
            Katup.setText(tbObat.getValueAt(tbObat.getSelectedRow(),11).toString());
            AnalisaSegmental.setText(tbObat.getValueAt(tbObat.getSelectedRow(),12).toString());
            Erap.setText(tbObat.getValueAt(tbObat.getSelectedRow(),13).toString());
            LainLain.setText(tbObat.getValueAt(tbObat.getSelectedRow(),14).toString());
            Kesimpulan.setText(tbObat.getValueAt(tbObat.getSelectedRow(),15).toString());
            // CUSTOM HASIL PEMERIKSAAN ECHO - HISTORY/CLINICAL DIAGNOSIS - Load 18 field baru dari tabel
            Aorta.setText(tbObat.getValueAt(tbObat.getSelectedRow(),16).toString());
            LeftAtrium.setText(tbObat.getValueAt(tbObat.getSelectedRow(),17).toString());
            RVDiastolic.setText(tbObat.getValueAt(tbObat.getSelectedRow(),18).toString());
            EjectionFraction.setText(tbObat.getValueAt(tbObat.getSelectedRow(),19).toString());
            EPSS.setText(tbObat.getValueAt(tbObat.getSelectedRow(),20).toString());
            LVOTDiameter.setText(tbObat.getValueAt(tbObat.getSelectedRow(),21).toString());
            LVEDD.setText(tbObat.getValueAt(tbObat.getSelectedRow(),22).toString());
            LVESD.setText(tbObat.getValueAt(tbObat.getSelectedRow(),23).toString());
            IVSDiastolic.setText(tbObat.getValueAt(tbObat.getSelectedRow(),24).toString());
            LVPWDiastolic.setText(tbObat.getValueAt(tbObat.getSelectedRow(),25).toString());
            TAPSE.setText(tbObat.getValueAt(tbObat.getSelectedRow(),26).toString());
            RVOT.setText(tbObat.getValueAt(tbObat.getSelectedRow(),27).toString());
            AorticSinus.setText(tbObat.getValueAt(tbObat.getSelectedRow(),28).toString());
            AscAorta.setText(tbObat.getValueAt(tbObat.getSelectedRow(),29).toString());
            AorticArcus.setText(tbObat.getValueAt(tbObat.getSelectedRow(),30).toString());
            DescAorta.setText(tbObat.getValueAt(tbObat.getSelectedRow(),31).toString());
            AbdominalAorta.setText(tbObat.getValueAt(tbObat.getSelectedRow(),32).toString());
            RVOTDiameter.setText(tbObat.getValueAt(tbObat.getSelectedRow(),33).toString());
            // CUSTOM HASIL PEMERIKSAAN ECHO - SPECTRAL DOPPLER - Load 11 field baru dari tabel
            DecTime.setText(tbObat.getValueAt(tbObat.getSelectedRow(),34).toString());
            EARatio.setText(tbObat.getValueAt(tbObat.getSelectedRow(),35).toString());
            TVG.setText(tbObat.getValueAt(tbObat.getSelectedRow(),36).toString());
            MPAP.setText(tbObat.getValueAt(tbObat.getSelectedRow(),37).toString());
            Emax.setText(tbObat.getValueAt(tbObat.getSelectedRow(),38).toString());
            AoVmax.setText(tbObat.getValueAt(tbObat.getSelectedRow(),39).toString());
            AoVTI.setText(tbObat.getValueAt(tbObat.getSelectedRow(),40).toString());
            TDLVEE.setText(tbObat.getValueAt(tbObat.getSelectedRow(),41).toString());
            TDLVEA.setText(tbObat.getValueAt(tbObat.getSelectedRow(),42).toString());
            IVCDiameter.setText(tbObat.getValueAt(tbObat.getSelectedRow(),43).toString());
            IVCRaP.setText(tbObat.getValueAt(tbObat.getSelectedRow(),44).toString());
            Valid.SetTgl2(Tanggal,tbObat.getValueAt(tbObat.getSelectedRow(),6).toString());
        }
    }

    private void isRawat() {
        try {
            ps=koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien, pasien.tgl_lahir,reg_periksa.tgl_registrasi "+
                    "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "+
                    "where reg_periksa.no_rawat=?");
            try {
                ps.setString(1,TNoRw.getText());
                rs=ps.executeQuery();
                if(rs.next()){
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : "+e);
        }
    }
 
    public void setNoRm(String norwt,Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);    
        isRawat(); 
        runBackground(() ->tampil());
    }
    
    public void isCek(){
        BtnSimpan.setEnabled(akses.gethasil_pemeriksaan_echo());
        BtnHapus.setEnabled(akses.gethasil_pemeriksaan_echo());
        BtnEdit.setEnabled(akses.gethasil_pemeriksaan_echo());
        BtnEdit.setEnabled(akses.gethasil_pemeriksaan_echo());
        if(akses.getjml2()>=1){
            KdDokter.setEditable(false);
            BtnDokter.setEnabled(false);
            KdDokter.setText(akses.getkode());
            Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", NmDokter,KdDokter.getText());
            if(NmDokter.getText().equals("")){
                KdDokter.setText("");
                JOptionPane.showMessageDialog(null,"User login bukan Dokter...!!");
            }
        }  
        
        if(TANGGALMUNDUR.equals("no")){
            if(!akses.getkode().equals("Admin Utama")){
                Tanggal.setEditable(false);
                Tanggal.setEnabled(false);
            }
        }
    }
    
    public void setTampil(){
       TabRawat.setSelectedIndex(1);
    }

    private void hapus() {
        if(Sequel.queryu2tf("delete from hasil_pemeriksaan_echo where no_rawat=?",1,new String[]{
            tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
        })==true){
            tabMode.removeRow(tbObat.getSelectedRow());
            LCount.setText(""+tabMode.getRowCount());
            TabRawat.setSelectedIndex(1);
        }else{
            JOptionPane.showMessageDialog(null,"Gagal menghapus..!!");
        }
    }

    private void ganti() {
        // CUSTOM HASIL PEMERIKSAAN ECHO - Update dengan 41 field (12 lama + 18 history + 11 spectral)
        if(Sequel.mengedittf("hasil_pemeriksaan_echo","no_rawat=?","no_rawat=?,tanggal=?,kd_dokter=?,sistolik=?,diastolic=?,kontraktilitas=?,dimensi_ruang=?,katup=?,"+
                "analisa_segmental=?,erap=?,lain_lain=?,kesimpulan=?,aorta=?,left_atrium=?,rv_diastolic=?,ejection_fraction=?,epss=?,lvot_diameter=?,"+
                "lv_edd=?,lv_esd=?,ivs_diastolic=?,lvpw_diastolic=?,tapse=?,rvot=?,aortic_sinus=?,asc_aorta=?,aortic_arcus=?,desc_aorta=?,abdominal_aorta=?,rvot_diameter=?,"+
                "dec_time=?,ea_ratio=?,tvg=?,m_pap=?,emax=?,ao_vmax=?,ao_vti=?,td_lv_ee=?,td_lv_ea=?,ivc_diameter=?,ivc_rap=?",42,new String[]{
                TNoRw.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Tanggal.getSelectedItem().toString().substring(11,19),KdDokter.getText(),
                Sistolik.getText(),Diastolik.getText(),Kontraktilitas.getText(),Dimensi.getText(),Katup.getText(),AnalisaSegmental.getText(),
                Erap.getText(),LainLain.getText(),Kesimpulan.getText(),
                Aorta.getText(),LeftAtrium.getText(),RVDiastolic.getText(),EjectionFraction.getText(),EPSS.getText(),LVOTDiameter.getText(),
                LVEDD.getText(),LVESD.getText(),IVSDiastolic.getText(),LVPWDiastolic.getText(),TAPSE.getText(),RVOT.getText(),
                AorticSinus.getText(),AscAorta.getText(),AorticArcus.getText(),DescAorta.getText(),AbdominalAorta.getText(),RVOTDiameter.getText(),
                DecTime.getText(),EARatio.getText(),TVG.getText(),MPAP.getText(),Emax.getText(),AoVmax.getText(),
                AoVTI.getText(),TDLVEE.getText(),TDLVEA.getText(),IVCDiameter.getText(),IVCRaP.getText(),
                tbObat.getValueAt(tbObat.getSelectedRow(),0).toString()
            })==true){
               tbObat.setValueAt(TNoRw.getText(),tbObat.getSelectedRow(),0);
                tbObat.setValueAt(TNoRM.getText(),tbObat.getSelectedRow(),1);
                tbObat.setValueAt(TPasien.getText(),tbObat.getSelectedRow(),2);
                tbObat.setValueAt(TglLahir.getText(),tbObat.getSelectedRow(),3);
                tbObat.setValueAt(KdDokter.getText(),tbObat.getSelectedRow(),4);
                tbObat.setValueAt(NmDokter.getText(),tbObat.getSelectedRow(),5);
                tbObat.setValueAt(Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Tanggal.getSelectedItem().toString().substring(11,19),tbObat.getSelectedRow(),6);
                tbObat.setValueAt(Sistolik.getText(),tbObat.getSelectedRow(),7);
                tbObat.setValueAt(Diastolik.getText(),tbObat.getSelectedRow(),8);
                tbObat.setValueAt(Kontraktilitas.getText(),tbObat.getSelectedRow(),9);
                tbObat.setValueAt(Dimensi.getText(),tbObat.getSelectedRow(),10);
                tbObat.setValueAt(Katup.getText(),tbObat.getSelectedRow(),11);
                tbObat.setValueAt(AnalisaSegmental.getText(),tbObat.getSelectedRow(),12);
                tbObat.setValueAt(Erap.getText(),tbObat.getSelectedRow(),13);
                tbObat.setValueAt(LainLain.getText(),tbObat.getSelectedRow(),14);
                tbObat.setValueAt(Kesimpulan.getText(),tbObat.getSelectedRow(),15);
                tbObat.setValueAt(Aorta.getText(),tbObat.getSelectedRow(),16);
                tbObat.setValueAt(LeftAtrium.getText(),tbObat.getSelectedRow(),17);
                tbObat.setValueAt(RVDiastolic.getText(),tbObat.getSelectedRow(),18);
                tbObat.setValueAt(EjectionFraction.getText(),tbObat.getSelectedRow(),19);
                tbObat.setValueAt(EPSS.getText(),tbObat.getSelectedRow(),20);
                tbObat.setValueAt(LVOTDiameter.getText(),tbObat.getSelectedRow(),21);
                tbObat.setValueAt(LVEDD.getText(),tbObat.getSelectedRow(),22);
                tbObat.setValueAt(LVESD.getText(),tbObat.getSelectedRow(),23);
                tbObat.setValueAt(IVSDiastolic.getText(),tbObat.getSelectedRow(),24);
                tbObat.setValueAt(LVPWDiastolic.getText(),tbObat.getSelectedRow(),25);
                tbObat.setValueAt(TAPSE.getText(),tbObat.getSelectedRow(),26);
                tbObat.setValueAt(RVOT.getText(),tbObat.getSelectedRow(),27);
                tbObat.setValueAt(AorticSinus.getText(),tbObat.getSelectedRow(),28);
                tbObat.setValueAt(AscAorta.getText(),tbObat.getSelectedRow(),29);
                tbObat.setValueAt(AorticArcus.getText(),tbObat.getSelectedRow(),30);
                tbObat.setValueAt(DescAorta.getText(),tbObat.getSelectedRow(),31);
                tbObat.setValueAt(AbdominalAorta.getText(),tbObat.getSelectedRow(),32);
                tbObat.setValueAt(RVOTDiameter.getText(),tbObat.getSelectedRow(),33);
                // CUSTOM HASIL PEMERIKSAAN ECHO - SPECTRAL DOPPLER - Update table display untuk 11 field
                tbObat.setValueAt(DecTime.getText(),tbObat.getSelectedRow(),34);
                tbObat.setValueAt(EARatio.getText(),tbObat.getSelectedRow(),35);
                tbObat.setValueAt(TVG.getText(),tbObat.getSelectedRow(),36);
                tbObat.setValueAt(MPAP.getText(),tbObat.getSelectedRow(),37);
                tbObat.setValueAt(Emax.getText(),tbObat.getSelectedRow(),38);
                tbObat.setValueAt(AoVmax.getText(),tbObat.getSelectedRow(),39);
                tbObat.setValueAt(AoVTI.getText(),tbObat.getSelectedRow(),40);
                tbObat.setValueAt(TDLVEE.getText(),tbObat.getSelectedRow(),41);
                tbObat.setValueAt(TDLVEA.getText(),tbObat.getSelectedRow(),42);
                tbObat.setValueAt(IVCDiameter.getText(),tbObat.getSelectedRow(),43);
                tbObat.setValueAt(IVCRaP.getText(),tbObat.getSelectedRow(),44);
                emptTeks();
                TabRawat.setSelectedIndex(1);
        }
    }
    
    private void isPhoto(){
        if(ChkAccor.isSelected()==true){
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(530,HEIGHT));
            TabData.setVisible(true);  
            ChkAccor.setVisible(true);
        }else if(ChkAccor.isSelected()==false){    
            ChkAccor.setVisible(false);
            PanelAccor.setPreferredSize(new Dimension(15,HEIGHT));
            TabData.setVisible(false);  
            ChkAccor.setVisible(true);
        }
    }

    private void panggilPhoto() {
        if(FormPhoto.isVisible()==true){
            try {
                ps=koneksi.prepareStatement("select hasil_pemeriksaan_echo_gambar.photo from hasil_pemeriksaan_echo_gambar where hasil_pemeriksaan_echo_gambar.no_rawat=?");
                try {
                    ps.setString(1,tbObat.getValueAt(tbObat.getSelectedRow(),0).toString());
                    rs=ps.executeQuery();
                    if(rs.next()){
                        if(rs.getString("photo").equals("")||rs.getString("photo").equals("-")){
                            LoadHTML2.setText("<html><body><center><br><br><font face='tahoma' size='2' color='#434343'>Kosong</font></center></body></html>");
                        }else{
                            LoadHTML2.setText("<html><body><center><a href='http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/hasilpemeriksaanecho/"+rs.getString("photo")+"'><img src='http://"+koneksiDB.HOSTHYBRIDWEB()+":"+koneksiDB.PORTWEB()+"/"+koneksiDB.HYBRIDWEB()+"/hasilpemeriksaanecho/"+rs.getString("photo")+"' alt='photo' width='550' height='550'/></a></center></body></html>");
                        }  
                    }else{
                        LoadHTML2.setText("<html><body><center><br><br><font face='tahoma' size='2' color='#434343'>Kosong</font></center></body></html>");
                    }
                } catch (Exception e) {
                    System.out.println("Notif : "+e);
                } finally{
                    if(rs!=null){
                        rs.close();
                    }
                    if(ps!=null){
                        ps.close();
                    }
                }
            } catch (Exception e) {
                System.out.println("Notif : "+e);
            }
        }
    }

    private void simpan() {
        // CUSTOM HASIL PEMERIKSAAN ECHO - HISTORY/CLINICAL DIAGNOSIS - Auto-fill dengan "-" jika kosong
        if(Aorta.getText().trim().equals("")){
            Aorta.setText("-");
        }
        if(LeftAtrium.getText().trim().equals("")){
            LeftAtrium.setText("-");
        }
        if(RVDiastolic.getText().trim().equals("")){
            RVDiastolic.setText("-");
        }
        if(EjectionFraction.getText().trim().equals("")){
            EjectionFraction.setText("-");
        }
        if(EPSS.getText().trim().equals("")){
            EPSS.setText("-");
        }
        if(LVOTDiameter.getText().trim().equals("")){
            LVOTDiameter.setText("-");
        }
        if(LVEDD.getText().trim().equals("")){
            LVEDD.setText("-");
        }
        if(LVESD.getText().trim().equals("")){
            LVESD.setText("-");
        }
        if(IVSDiastolic.getText().trim().equals("")){
            IVSDiastolic.setText("-");
        }
        if(LVPWDiastolic.getText().trim().equals("")){
            LVPWDiastolic.setText("-");
        }
        if(TAPSE.getText().trim().equals("")){
            TAPSE.setText("-");
        }
        if(RVOT.getText().trim().equals("")){
            RVOT.setText("-");
        }
        if(AorticSinus.getText().trim().equals("")){
            AorticSinus.setText("-");
        }
        if(AscAorta.getText().trim().equals("")){
            AscAorta.setText("-");
        }
        if(AorticArcus.getText().trim().equals("")){
            AorticArcus.setText("-");
        }
        if(DescAorta.getText().trim().equals("")){
            DescAorta.setText("-");
        }
        if(AbdominalAorta.getText().trim().equals("")){
            AbdominalAorta.setText("-");
        }
        if(RVOTDiameter.getText().trim().equals("")){
            RVOTDiameter.setText("-");
        }
        // CUSTOM HASIL PEMERIKSAAN ECHO - SPECTRAL DOPPLER - Auto-fill dengan "-" jika kosong
        if(DecTime.getText().trim().equals("")){
            DecTime.setText("-");
        }
        if(EARatio.getText().trim().equals("")){
            EARatio.setText("-");
        }
        if(TVG.getText().trim().equals("")){
            TVG.setText("-");
        }
        if(MPAP.getText().trim().equals("")){
            MPAP.setText("-");
        }
        if(Emax.getText().trim().equals("")){
            Emax.setText("-");
        }
        if(AoVmax.getText().trim().equals("")){
            AoVmax.setText("-");
        }
        if(AoVTI.getText().trim().equals("")){
            AoVTI.setText("-");
        }
        if(TDLVEE.getText().trim().equals("")){
            TDLVEE.setText("-");
        }
        if(TDLVEA.getText().trim().equals("")){
            TDLVEA.setText("-");
        }
        if(IVCDiameter.getText().trim().equals("")){
            IVCDiameter.setText("-");
        }
        if(IVCRaP.getText().trim().equals("")){
            IVCRaP.setText("-");
        }
        // CUSTOM HASIL PEMERIKSAAN ECHO - Simpan dengan 41 field (12 original + 18 history + 11 spectral)
        if(Sequel.menyimpantf("hasil_pemeriksaan_echo","?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?","No.Rawat",41,new String[]{
                TNoRw.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Tanggal.getSelectedItem().toString().substring(11,19),KdDokter.getText(),
                Sistolik.getText(),Diastolik.getText(),Kontraktilitas.getText(),Dimensi.getText(),Katup.getText(),AnalisaSegmental.getText(),
                Erap.getText(),LainLain.getText(),Kesimpulan.getText(),
                Aorta.getText(),LeftAtrium.getText(),RVDiastolic.getText(),EjectionFraction.getText(),EPSS.getText(),LVOTDiameter.getText(),
                LVEDD.getText(),LVESD.getText(),IVSDiastolic.getText(),LVPWDiastolic.getText(),TAPSE.getText(),RVOT.getText(),
                AorticSinus.getText(),AscAorta.getText(),AorticArcus.getText(),DescAorta.getText(),AbdominalAorta.getText(),RVOTDiameter.getText(),
                DecTime.getText(),EARatio.getText(),TVG.getText(),MPAP.getText(),Emax.getText(),AoVmax.getText(),
                AoVTI.getText(),TDLVEE.getText(),TDLVEA.getText(),IVCDiameter.getText(),IVCRaP.getText()
            })==true){
                tabMode.addRow(new Object[]{
                    TNoRw.getText(),TNoRM.getText(),TPasien.getText(),TglLahir.getText(),KdDokter.getText(),NmDokter.getText(),Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Tanggal.getSelectedItem().toString().substring(11,19),
                    Sistolik.getText(),Diastolik.getText(),Kontraktilitas.getText(),Dimensi.getText(),Katup.getText(),AnalisaSegmental.getText(),Erap.getText(),LainLain.getText(),Kesimpulan.getText(),
                    Aorta.getText(),LeftAtrium.getText(),RVDiastolic.getText(),EjectionFraction.getText(),EPSS.getText(),LVOTDiameter.getText(),
                    LVEDD.getText(),LVESD.getText(),IVSDiastolic.getText(),LVPWDiastolic.getText(),TAPSE.getText(),RVOT.getText(),
                    AorticSinus.getText(),AscAorta.getText(),AorticArcus.getText(),DescAorta.getText(),AbdominalAorta.getText(),RVOTDiameter.getText(),
                    DecTime.getText(),EARatio.getText(),TVG.getText(),MPAP.getText(),Emax.getText(),AoVmax.getText(),
                    AoVTI.getText(),TDLVEE.getText(),TDLVEA.getText(),IVCDiameter.getText(),IVCRaP.getText()
                });
                emptTeks();
                LCount.setText(""+tabMode.getRowCount());
        }
    }
    
    private void tampilOrthanc() {
        if(TabData.isVisible()==true){
            if(tbObat.getSelectedRow()!= -1){
                 if(TabData.getSelectedIndex()==1){
                     try {
                         Valid.tabelKosong(tabModeDicom);
                         ApiOrthanc orthanc=new ApiOrthanc();
                         root=orthanc.AmbilSeries(tbObat.getValueAt(tbObat.getSelectedRow(),1).toString(),Valid.SetTgl(DTPCari1.getSelectedItem()+"").replaceAll("-",""),Valid.SetTgl(DTPCari2.getSelectedItem()+"").replaceAll("-",""));
                         for(JsonNode list:root){
                             for(JsonNode sublist:list.path("Series")){
                                  tabModeDicom.addRow(new Object[]{
                                       list.path("PatientMainDicomTags").path("PatientID").asText(),list.path("ID").asText(),sublist.asText()
                                  });   
                             }        
                         }
                     } catch (Exception e) {
                         System.out.println("Notif : "+e);
                     }
                 }
            }
        }
    }
    
    private void runBackground(Runnable task) {
        if (ceksukses) return;
        if (executor.isShutdown() || executor.isTerminated()) return;
        if (!isDisplayable()) return;

        ceksukses = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    ceksukses = false;
                    SwingUtilities.invokeLater(() -> {
                        if (isDisplayable()) {
                            setCursor(Cursor.getDefaultCursor());
                        }
                    });
                }
            });
        } catch (RejectedExecutionException ex) {
            ceksukses = false;
        }
    }
    
    @Override
    public void dispose() {
        executor.shutdownNow();
        super.dispose();
    }
}

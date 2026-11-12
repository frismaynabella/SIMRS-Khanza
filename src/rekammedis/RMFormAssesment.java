/*
 * Form Re-Assessment Rehabilitasi Medik
 * Digunakan untuk melakukan re-assessment pasien rehabilitasi medik
 */

package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import kepegawaian.DlgCariPegawai;
//import rekammedis.RMCariDataDiagnosa;

/**
 *
 * @author perpustakaan
 */
public final class RMFormAssesment extends javax.swing.JDialog {

    private final DefaultTableModel tabMode;
    private Connection koneksi = koneksiDB.condb();
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i = 0, pilihan = 0;
    private DlgCariDokter dokter = new DlgCariDokter(null, false);
    private DlgCariPegawai pegawai = new DlgCariPegawai(null, false);
    //private RMCariDataDiagnosa icd10 = new RMCariDataDiagnosa(null, false);
    private String finger = "";

    /**
     * Creates new form DlgRujuk
     *
     * @param parent
     * @param modal
     */
    public RMFormAssesment(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8, 1);
        setSize(628, 674);

        tabMode = new DefaultTableModel(null, new Object[]{
            "No Rawat", "RM", "Nama", "JK", "Lahir", "Umur", "Tanggal", "KD", "Dokter", "Subjective", "Objective", "Assesment", 
            "Goal", "Tindakan", "Edukasi", "Frekuensi", "Rencana"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbObat.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 17; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(100);
            } else if (i == 1) {
                column.setPreferredWidth(100);
            } else if (i == 2) {
                column.setPreferredWidth(160);
            } else if (i == 3) {
                column.setPreferredWidth(100);
            } else if (i == 4) {
                column.setPreferredWidth(100);
            } else if (i == 5) {
                column.setPreferredWidth(120);
            } else if (i == 6) {
                column.setPreferredWidth(150);
            } else if (i == 7) {
                column.setPreferredWidth(150);
            } else if (i == 8) {
                column.setPreferredWidth(150);
            } else if (i == 9) {
                column.setPreferredWidth(150);
            } else if (i == 10) {
                column.setPreferredWidth(200);
            } else if (i == 11) {
                column.setPreferredWidth(150);
            } else if (i == 12) {
                column.setPreferredWidth(200);
            } else if (i == 13) {
                column.setPreferredWidth(200);
            } else if (i == 14) {
                column.setPreferredWidth(200);
            } else if (i == 15) {
                column.setPreferredWidth(200);
            } else if (i == 16) {
                column.setPreferredWidth(200);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        TNoRw.setDocument(new batasInput((byte) 17).getKata(TNoRw));
        KdDok.setDocument(new batasInput((byte) 20).getKata(KdDok));

        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
                    }
                }
            });
        }
        
        
        
        dokter.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (dokter.getTable().getSelectedRow() != -1) {
                    KdDok.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 0).toString());
                    TDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 1).toString());
                }
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

        ChkInput.setSelected(false);
        isForm();

        jam();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnUjiFungsi = new javax.swing.JMenuItem();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        PanelInput = new javax.swing.JPanel();
        FormInput = new widget.PanelBiasa();
        jLabel4 = new widget.Label();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        Tanggal = new widget.Tanggal();
        TNoRM = new widget.TextBox();
        jLabel16 = new widget.Label();
        Jam = new widget.ComboBox();
        Menit = new widget.ComboBox();
        Detik = new widget.ComboBox();
        ChkKejadian = new widget.CekBox();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        JK = new widget.TextBox();
        KdDok = new widget.TextBox();
        TDokter = new widget.TextBox();
        btnDokter1 = new widget.Button();
        jLabel9 = new widget.Label();
        jLabel10 = new widget.Label();
        umur = new widget.TextBox();
        scrollPane2 = new widget.ScrollPane();
        O = new widget.TextArea();
        scrollPane3 = new widget.ScrollPane();
        S = new widget.TextArea();
        scrollPane6 = new widget.ScrollPane();
        A = new widget.TextArea();
        jLabel22 = new widget.Label();
        jLabel23 = new widget.Label();
        jLabel24 = new widget.Label();
        jLabel25 = new widget.Label();
        goal = new widget.TextBox();
        jLabel26 = new widget.Label();
        jLabel27 = new widget.Label();
        scrollPane7 = new widget.ScrollPane();
        tindakan = new widget.TextArea();
        jLabel28 = new widget.Label();
        edukasi = new widget.TextBox();
        jLabel29 = new widget.Label();
        frekuensi = new widget.TextBox();
        rencana = new widget.TextBox();
        jLabel30 = new widget.Label();
        jLabel31 = new widget.Label();
        ChkInput = new widget.CekBox();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnUjiFungsi.setBackground(new java.awt.Color(255, 255, 254));
        MnUjiFungsi.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnUjiFungsi.setForeground(new java.awt.Color(50, 50, 50));
        MnUjiFungsi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnUjiFungsi.setText("Cetak Form");
        MnUjiFungsi.setName("MnUjiFungsi"); // NOI18N
        MnUjiFungsi.setPreferredSize(new java.awt.Dimension(270, 26));
        MnUjiFungsi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnUjiFungsiActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnUjiFungsi);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Form Re Assesment ]::", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 100));

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

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 100));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 44));
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
        BtnPrint.setEnabled(false);
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

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass8.add(LCount);

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

        jPanel3.add(panelGlass8, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tanggal :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(55, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "24-10-2025" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "24-10-2025" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(320, 23));
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

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(28, 23));
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
        panelGlass9.add(BtnAll);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(275, 300));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput.setBackground(new java.awt.Color(250, 255, 245));
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(275, 300));
        FormInput.setLayout(null);

        jLabel4.setText("No.Rawat :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(0, 10, 75, 23);

        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(79, 10, 140, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        TPasien.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TPasienKeyPressed(evt);
            }
        });
        FormInput.add(TPasien);
        TPasien.setBounds(340, 10, 270, 23);

        Tanggal.setForeground(new java.awt.Color(50, 70, 50));
        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "24-10-2025" }));
        Tanggal.setDisplayFormat("dd-MM-yyyy");
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.setOpaque(false);
        Tanggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKeyPressed(evt);
            }
        });
        FormInput.add(Tanggal);
        Tanggal.setBounds(480, 40, 90, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        TNoRM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRMKeyPressed(evt);
            }
        });
        FormInput.add(TNoRM);
        TNoRM.setBounds(222, 10, 112, 23);

        jLabel16.setText("Tanggal :");
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel16);
        jLabel16.setBounds(415, 40, 60, 23);

        Jam.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        Jam.setName("Jam"); // NOI18N
        Jam.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JamKeyPressed(evt);
            }
        });
        FormInput.add(Jam);
        Jam.setBounds(580, 40, 62, 23);

        Menit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        Menit.setName("Menit"); // NOI18N
        Menit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MenitKeyPressed(evt);
            }
        });
        FormInput.add(Menit);
        Menit.setBounds(640, 40, 62, 23);

        Detik.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        Detik.setName("Detik"); // NOI18N
        Detik.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DetikKeyPressed(evt);
            }
        });
        FormInput.add(Detik);
        Detik.setBounds(700, 40, 62, 23);

        ChkKejadian.setBorder(null);
        ChkKejadian.setSelected(true);
        ChkKejadian.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        ChkKejadian.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkKejadian.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkKejadian.setName("ChkKejadian"); // NOI18N
        FormInput.add(ChkKejadian);
        ChkKejadian.setBounds(770, 40, 23, 23);

        jLabel8.setText("Jk :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(575, 10, 80, 23);

        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        FormInput.add(TglLahir);
        TglLahir.setBounds(80, 40, 120, 23);

        JK.setEditable(false);
        JK.setHighlighter(null);
        JK.setName("JK"); // NOI18N
        JK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JKActionPerformed(evt);
            }
        });
        JK.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JKKeyPressed(evt);
            }
        });
        FormInput.add(JK);
        JK.setBounds(660, 10, 140, 23);

        KdDok.setHighlighter(null);
        KdDok.setName("KdDok"); // NOI18N
        KdDok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                KdDokActionPerformed(evt);
            }
        });
        KdDok.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdDokKeyPressed(evt);
            }
        });
        FormInput.add(KdDok);
        KdDok.setBounds(80, 70, 60, 23);

        TDokter.setEditable(false);
        TDokter.setHighlighter(null);
        TDokter.setName("TDokter"); // NOI18N
        TDokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TDokterKeyPressed(evt);
            }
        });
        FormInput.add(TDokter);
        TDokter.setBounds(140, 70, 250, 23);

        btnDokter1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnDokter1.setMnemonic('2');
        btnDokter1.setToolTipText("Alt+2");
        btnDokter1.setName("btnDokter1"); // NOI18N
        btnDokter1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDokter1ActionPerformed(evt);
            }
        });
        FormInput.add(btnDokter1);
        btnDokter1.setBounds(390, 70, 28, 23);

        jLabel9.setText("Tgl.Lahir :");
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(15, 40, 60, 23);

        jLabel10.setText("Umur :");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(185, 40, 80, 23);

        umur.setEditable(false);
        umur.setHighlighter(null);
        umur.setName("umur"); // NOI18N
        umur.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                umurKeyPressed(evt);
            }
        });
        FormInput.add(umur);
        umur.setBounds(270, 40, 120, 23);

        scrollPane2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane2.setName("scrollPane2"); // NOI18N

        O.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        O.setColumns(20);
        O.setRows(5);
        O.setName("O"); // NOI18N
        O.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                OKeyPressed(evt);
            }
        });
        scrollPane2.setViewportView(O);

        FormInput.add(scrollPane2);
        scrollPane2.setBounds(20, 210, 380, 60);

        scrollPane3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane3.setName("scrollPane3"); // NOI18N

        S.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        S.setColumns(20);
        S.setRows(5);
        S.setName("S"); // NOI18N
        S.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SKeyPressed(evt);
            }
        });
        scrollPane3.setViewportView(S);

        FormInput.add(scrollPane3);
        scrollPane3.setBounds(20, 120, 380, 60);

        scrollPane6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane6.setName("scrollPane6"); // NOI18N

        A.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        A.setColumns(20);
        A.setRows(5);
        A.setName("A"); // NOI18N
        A.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AKeyPressed(evt);
            }
        });
        scrollPane6.setViewportView(A);

        FormInput.add(scrollPane6);
        scrollPane6.setBounds(420, 120, 380, 60);

        jLabel22.setText("Dokter :");
        jLabel22.setName("jLabel22"); // NOI18N
        FormInput.add(jLabel22);
        jLabel22.setBounds(5, 70, 70, 23);

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel23.setText("a. Goal Of Treatment : ");
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(830, 110, 150, 23);

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel24.setText("Subjective :");
        jLabel24.setName("jLabel24"); // NOI18N
        FormInput.add(jLabel24);
        jLabel24.setBounds(20, 100, 80, 23);

        jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel25.setText("Assesment :");
        jLabel25.setName("jLabel25"); // NOI18N
        FormInput.add(jLabel25);
        jLabel25.setBounds(420, 100, 80, 23);

        goal.setFocusTraversalPolicyProvider(true);
        goal.setName("goal"); // NOI18N
        goal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                goalKeyPressed(evt);
            }
        });
        FormInput.add(goal);
        goal.setBounds(990, 110, 250, 23);

        jLabel26.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel26.setText("Objective :");
        jLabel26.setName("jLabel26"); // NOI18N
        FormInput.add(jLabel26);
        jLabel26.setBounds(20, 190, 80, 23);

        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel27.setText("b. Tindakan / Program :");
        jLabel27.setName("jLabel27"); // NOI18N
        FormInput.add(jLabel27);
        jLabel27.setBounds(830, 135, 150, 23);

        scrollPane7.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane7.setName("scrollPane7"); // NOI18N

        tindakan.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tindakan.setColumns(20);
        tindakan.setRows(5);
        tindakan.setName("tindakan"); // NOI18N
        tindakan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tindakanKeyPressed(evt);
            }
        });
        scrollPane7.setViewportView(tindakan);

        FormInput.add(scrollPane7);
        scrollPane7.setBounds(830, 155, 410, 60);

        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel28.setText("c. Edukasi : ");
        jLabel28.setName("jLabel28"); // NOI18N
        FormInput.add(jLabel28);
        jLabel28.setBounds(830, 220, 150, 23);

        edukasi.setFocusTraversalPolicyProvider(true);
        edukasi.setName("edukasi"); // NOI18N
        edukasi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                edukasiKeyPressed(evt);
            }
        });
        FormInput.add(edukasi);
        edukasi.setBounds(990, 220, 250, 23);

        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel29.setText("d. Frekuensi Kunjungan :");
        jLabel29.setName("jLabel29"); // NOI18N
        FormInput.add(jLabel29);
        jLabel29.setBounds(830, 250, 150, 23);

        frekuensi.setFocusTraversalPolicyProvider(true);
        frekuensi.setName("frekuensi"); // NOI18N
        frekuensi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                frekuensiKeyPressed(evt);
            }
        });
        FormInput.add(frekuensi);
        frekuensi.setBounds(990, 250, 250, 23);

        rencana.setFocusTraversalPolicyProvider(true);
        rencana.setName("rencana"); // NOI18N
        rencana.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                rencanaKeyPressed(evt);
            }
        });
        FormInput.add(rencana);
        rencana.setBounds(420, 210, 380, 23);

        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel30.setText("Rencana Tindak Lanjut (EVALUASI/RUJUK/SELESAI) :");
        jLabel30.setName("jLabel30"); // NOI18N
        FormInput.add(jLabel30);
        jLabel30.setBounds(420, 190, 310, 23);

        jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel31.setText("Planning :");
        jLabel31.setName("jLabel31"); // NOI18N
        FormInput.add(jLabel31);
        jLabel31.setBounds(830, 90, 150, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('I');
        ChkInput.setText(".: Input Data");
        ChkInput.setToolTipText("Alt+I");
        ChkInput.setBorderPainted(true);
        ChkInput.setBorderPaintedFlat(true);
        ChkInput.setFocusable(false);
        ChkInput.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ChkInput.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkInput.setName("ChkInput"); // NOI18N
        ChkInput.setPreferredSize(new java.awt.Dimension(192, 20));
        ChkInput.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkInputActionPerformed(evt);
            }
        });
        PanelInput.add(ChkInput, java.awt.BorderLayout.PAGE_END);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
        internalFrame1.getAccessibleContext().setAccessibleName("::[ Lembar Re Assesment ]::");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
//        if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
//            isRawat();
//            isPsien();
//        } else {
//            Valid.pindah(evt, TCari, Tanggal);
//        }
}//GEN-LAST:event_TNoRwKeyPressed

    private void TPasienKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TPasienKeyPressed
        Valid.pindah(evt, TCari, BtnSimpan);
}//GEN-LAST:event_TPasienKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
            Valid.textKosong(TNoRw, "Pasien");
        } else if (KdDok.getText().trim().equals("") || TDokter.getText().trim().equals("")) {
            Valid.textKosong(KdDok, "Dokter");
        } else if (S.getText().trim().equals("")) {
            Valid.textKosong(S, "Subjective");
        } else if (O.getText().trim().equals("")) {
            Valid.textKosong(O, "Objective");
        } else if (A.getText().trim().equals("")) {
            Valid.textKosong(A, "Assesment");
        } else if (goal.getText().trim().equals("")) {
            Valid.textKosong(goal, "Goal");
        } else if (tindakan.getText().trim().equals("")) {
            Valid.textKosong(tindakan, "Tindakan");
        } else if (edukasi.getText().trim().equals("")) {
            Valid.textKosong(edukasi, "Edukasi");
        } else if (frekuensi.getText().trim().equals("")) {
            Valid.textKosong(frekuensi, "Frekuensi");
        } else if (rencana.getText().trim().equals("")) {
            Valid.textKosong(rencana, "Rencana");
        } else {
            String tgldaftar = Sequel.cariIsi("select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText());
            if(Sequel.cekTanggalRegistrasi(tgldaftar,Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem())==true){
                if (akses.getkode().equals("Admin Utama")) {
                    if (Sequel.menyimpantf("lembar_reassesment", "?,?,?,?,?,?,?,?,?,?,?", "Data", 11, new String[]{
                        TNoRw.getText(), S.getText().trim(), O.getText().trim(), A.getText().trim(), goal.getText().trim(), tindakan.getText().trim(),
                        edukasi.getText().trim(), frekuensi.getText().trim(), rencana.getText(),
                        Valid.SetTgl(Tanggal.getSelectedItem() + "") + " " + Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(),
                        KdDok.getText()
                    }) == true) {
                        JOptionPane.showMessageDialog(rootPane, "Berhasil Simpan !!");
                        tampil();
                        emptTeks();
                    }
                } else if (KdDok.getText().equals(akses.getkode())) {
                    if (Sequel.menyimpantf("lembar_reassesment", "?,?,?,?,?,?,?,?,?,?,?", "Data", 11, new String[]{
                        TNoRw.getText(), S.getText().trim(), O.getText().trim(), A.getText().trim(), goal.getText().trim(), tindakan.getText().trim(),
                        edukasi.getText().trim(), frekuensi.getText().trim(), rencana.getText(),
                        Valid.SetTgl(Tanggal.getSelectedItem() + "") + " " + Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(),
                        KdDok.getText()
                    }) == true) {
                        JOptionPane.showMessageDialog(rootPane, "Berhasil Simpan !!");
                        tampil();
                        emptTeks();
                    }
                }else{
                    JOptionPane.showMessageDialog(rootPane, "Hanya bisa dibuat oleh dokter yang bersangkutan..!!");
                } 
            }        
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnSimpanActionPerformed(null);
        } else {
            //Valid.pindah(evt,cmbSkor3,BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
        ChkInput.setSelected(true);
        isForm();
}//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            emptTeks();
        } else {
            Valid.pindah(evt, BtnSimpan, BtnHapus);
        }
}//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if (tbObat.getSelectedRow() != -1) {
            int konfirmasi = JOptionPane.showConfirmDialog(
                null,
                "Apakah Anda yakin ingin menghapus data ini?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (konfirmasi == JOptionPane.YES_OPTION) {
                if (akses.getkode().equals("Admin Utama")) {
                    hapus();
                } else {
                    if (KdDok.getText().equals(akses.getkode())) {
                        hapus();
                    } else {
                        JOptionPane.showMessageDialog(rootPane, "Hanya bisa dihapus oleh dokter yang bersangkutan..!!");
                    }
                }
            }
            
        } else {
            JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
        }

}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnHapusActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnBatal, BtnEdit);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
            Valid.textKosong(TNoRw, "Pasien");
        } else if (KdDok.getText().trim().equals("") || TDokter.getText().trim().equals("")) {
            Valid.textKosong(KdDok, "Dokter");
        } else {
            if (tbObat.getSelectedRow() > -1) {
                String tgldaftar = Sequel.cariIsi("select concat(reg_periksa.tgl_registrasi,' ',reg_periksa.jam_reg) from reg_periksa where reg_periksa.no_rawat=?",TNoRw.getText());
                if(Sequel.cekTanggalRegistrasi(tgldaftar,Valid.SetTgl(Tanggal.getSelectedItem()+"")+" "+Jam.getSelectedItem()+":"+Menit.getSelectedItem()+":"+Detik.getSelectedItem())==true){
                    if (akses.getkode().equals("Admin Utama")) {
                        ganti();
                        JOptionPane.showMessageDialog(rootPane, "Berhasil Edit..!!");
                    } else {
                        if (KdDok.getText().equals(akses.getkode())) {
                            ganti();
                            JOptionPane.showMessageDialog(rootPane, "Berhasil Edit..!!");
                        } else {
                            JOptionPane.showMessageDialog(rootPane, "Hanya bisa diganti oleh dokter yang bersangkutan..!!");
                        }
                    }
                }    
            } else {
                JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
            }
        }
}//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnEditActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnHapus, BtnPrint);
        }
}//GEN-LAST:event_BtnEditKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dokter.dispose();
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnKeluarActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnEdit, TCari);
        }
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(rootPane, "Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            BtnBatal.requestFocus();
        } else if (tabMode.getRowCount() != 0) {
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            Valid.MyReportqry("rptFormAssesment.jasper", "report", "::[ Form Assessment ]::",
                    "SELECT\n" +
                    "    reg_periksa.no_rawat,\n" +
                    "		reg_periksa.no_rkm_medis,\n" +
                    "		pasien.nm_pasien,\n" +
                    "		pasien.jk,\n" +
                    "		pasien.tgl_lahir,\n" +
                    "		pasien.alamat,\n" +
                    "		CONCAT(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,\n" +
                    "		lembar_reassesment.tanggal,\n" +
                    "		lembar_reassesment.kd_dokter,\n" +
                    "		dokter.nm_dokter,\n" +
                    "		lembar_reassesment.subjective,\n" +
                    "		lembar_reassesment.objective,\n" +
                    "		lembar_reassesment.assesment,\n" +
                    "		lembar_reassesment.goal,\n" +
                    "		lembar_reassesment.tindakan,\n" +
                    "		lembar_reassesment.edukasi,\n" +
                    "		lembar_reassesment.frekuensi,\n" +
                    "		lembar_reassesment.rencana\n" +
                    "FROM\n" +
                    "    reg_periksa\n" +
                    "    INNER JOIN lembar_reassesment ON reg_periksa.no_rawat = lembar_reassesment.no_rawat\n" +
                    "    INNER JOIN dokter ON lembar_reassesment.kd_dokter = dokter.kd_dokter\n" +
                    "    INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis\n" +
                    "WHERE\n" +
                    "		lembar_reassesment.no_rawat = '" + tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString() + "'\n" +
                    "       AND lembar_reassesment.tanggal = '" + tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString() + "'"
                    + " order by lembar_reassesment.tanggal DESC", param);
        }
        this.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnPrintActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnEdit, BtnKeluar);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            BtnCariActionPerformed(null);
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            BtnCari.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            BtnKeluar.requestFocus();
        }
}//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        tampil();
}//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnCariActionPerformed(null);
        } else {
            Valid.pindah(evt, TCari, BtnAll);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        tampil();
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            tampil();
            TCari.setText("");
        } else {
            Valid.pindah(evt, BtnCari, TPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void TanggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalKeyPressed
        Valid.pindah(evt, TCari, Jam);
}//GEN-LAST:event_TanggalKeyPressed

    private void TNoRMKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRMKeyPressed
        // Valid.pindah(evt, TNm, BtnSimpan);
}//GEN-LAST:event_TNoRMKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if (tabMode.getRowCount() != 0) {
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbObatMouseClicked

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyPressed
        if (tabMode.getRowCount() != 0) {
            if ((evt.getKeyCode() == KeyEvent.VK_ENTER) || (evt.getKeyCode() == KeyEvent.VK_UP) || (evt.getKeyCode() == KeyEvent.VK_DOWN)) {
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
}//GEN-LAST:event_tbObatKeyPressed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void JamKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JamKeyPressed
        Valid.pindah(evt, Tanggal, Menit);
    }//GEN-LAST:event_JamKeyPressed

    private void MenitKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MenitKeyPressed
        Valid.pindah(evt, Jam, Detik);
    }//GEN-LAST:event_MenitKeyPressed

    private void DetikKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DetikKeyPressed
        //Valid.pindah(evt, Menit, btnPetugas);
    }//GEN-LAST:event_DetikKeyPressed

    private void MnUjiFungsiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnUjiFungsiActionPerformed
        if (tbObat.getSelectedRow() > -1) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            Valid.MyReportqry("rptFormAssesment.jasper", "report", "::[ Form Assessment ]::",
                    "SELECT\n" +
                    "    reg_periksa.no_rawat,\n" +
                    "		reg_periksa.no_rkm_medis,\n" +
                    "		pasien.nm_pasien,\n" +
                    "		pasien.jk,\n" +
                    "		pasien.tgl_lahir,\n" +
                    "		pasien.alamat,\n" +
                    "		CONCAT(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,\n" +
                    "		lembar_reassesment.tanggal,\n" +
                    "		lembar_reassesment.kd_dokter,\n" +
                    "		dokter.nm_dokter,\n" +
                    "		lembar_reassesment.subjective,\n" +
                    "		lembar_reassesment.objective,\n" +
                    "		lembar_reassesment.assesment,\n" +
                    "		lembar_reassesment.goal,\n" +
                    "		lembar_reassesment.tindakan,\n" +
                    "		lembar_reassesment.edukasi,\n" +
                    "		lembar_reassesment.frekuensi,\n" +
                    "		lembar_reassesment.rencana\n" +
                    "FROM\n" +
                    "    reg_periksa\n" +
                    "    INNER JOIN lembar_reassesment ON reg_periksa.no_rawat = lembar_reassesment.no_rawat\n" +
                    "    INNER JOIN dokter ON lembar_reassesment.kd_dokter = dokter.kd_dokter\n" +
                    "    INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis\n" +
                    "WHERE\n" +
                    "		lembar_reassesment.no_rawat = '" + tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString() + "'\n" +
                    "       AND lembar_reassesment.tanggal = '" + tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString() + "'"
                    + " order by lembar_reassesment.tanggal DESC", param);
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_MnUjiFungsiActionPerformed

    public void cetakPDFLaporanKFR(String norawat, String norm) {
        String kodedokter = Sequel.cariIsi("SELECT\n"
                + "	laporan_program_kfr.kd_dokter\n"
                + "FROM\n"
                + "	laporan_program_kfr\n"
                + "	INNER JOIN\n"
                + "	dokter\n"
                + "	ON \n"
                + "		laporan_program_kfr.kd_dokter = dokter.kd_dokter where laporan_program_kfr.no_rawat='" + norawat + "'");
        String namadokter = Sequel.cariIsi("SELECT\n"
                + "	dokter.nm_dokter\n"
                + "FROM\n"
                + "	laporan_program_kfr\n"
                + "	INNER JOIN\n"
                + "	dokter\n"
                + "	ON \n"
                + "		laporan_program_kfr.kd_dokter = dokter.kd_dokter where laporan_program_kfr.no_rawat='" + norawat + "'");
        String tanggal = Sequel.cariIsi("SELECT\n"
                + "	laporan_program_kfr.tanggal\n"
                + "FROM\n"
                + "	laporan_program_kfr\n"
                + "	where laporan_program_kfr.no_rawat='" + norawat + "'");
        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
        finger = Sequel.cariIsi("SELECT sha1( sidikjari.sidikjari ) FROM sidikjari INNER JOIN dokter INNER JOIN pegawai ON sidikjari.id = pegawai.id  AND dokter.kd_dokter = pegawai.nik  WHERE dokter.kd_dokter = ?", kodedokter);
        param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + namadokter + "\nID " + (finger.equals("") ? kodedokter : finger) + "\n" + tanggal);
        Valid.MyReportqry("rptLaporanProgramKFR.jasper", "report", "::[ Laporan Pelaksanaan Program Rehabilitasi ]::",
                "SELECT\n"
                + "	laporan_program_kfr.no_rawat,\n"
                + "	laporan_program_kfr.no_rkm_medis,\n"
                + "	laporan_program_kfr.tanggal,\n"
                + "	laporan_program_kfr.diagnosa,\n"
                + "	laporan_program_kfr.tkm,\n"
                + "	laporan_program_kfr.saran,\n"
                + "	laporan_program_kfr.nik,\n"
                + "	laporan_program_kfr.kd_dokter,\n"
                + "	pasien.jk,\n"
                + "	pasien.nm_pasien,\n"
                + "	pasien.tmp_lahir,\n"
                + "	pasien.tgl_lahir,\n"
                + "	pasien.umur,\n"
                + "	pasien.alamat,\n"
                + "	pasien.no_ktp,\n"
                + "	pegawai.nama,\n"
                + "	dokter.nm_dokter \n"
                + "FROM\n"
                + "	laporan_program_kfr\n"
                + "	INNER JOIN pasien ON laporan_program_kfr.no_rkm_medis = pasien.no_rkm_medis\n"
                + "	INNER JOIN pegawai ON laporan_program_kfr.nik = pegawai.nik\n"
                + "	INNER JOIN dokter ON laporan_program_kfr.kd_dokter = dokter.kd_dokter \n"
                + "WHERE\n"
                + "	laporan_program_kfr.no_rawat ='" + norawat + "' and laporan_program_kfr.tanggal ='" + tanggal + "'", param);

    }

    public void cetakPDFLaporanKFRGabung(String norawat, String norm, String norawatuntuknamafile) {
        String kodedokter = Sequel.cariIsi("SELECT\n"
                + "	laporan_program_kfr.kd_dokter\n"
                + "FROM\n"
                + "	laporan_program_kfr\n"
                + "	INNER JOIN\n"
                + "	dokter\n"
                + "	ON \n"
                + "		laporan_program_kfr.kd_dokter = dokter.kd_dokter where laporan_program_kfr.no_rawat='" + norawat + "'");
        String namadokter = Sequel.cariIsi("SELECT\n"
                + "	dokter.nm_dokter\n"
                + "FROM\n"
                + "	laporan_program_kfr\n"
                + "	INNER JOIN\n"
                + "	dokter\n"
                + "	ON \n"
                + "		laporan_program_kfr.kd_dokter = dokter.kd_dokter where laporan_program_kfr.no_rawat='" + norawat + "'");
        String tanggal = Sequel.cariIsi("SELECT\n"
                + "	laporan_program_kfr.tanggal\n"
                + "FROM\n"
                + "	laporan_program_kfr\n"
                + "	where laporan_program_kfr.no_rawat='" + norawat + "'");
        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
        finger = Sequel.cariIsi("SELECT sha1( sidikjari.sidikjari ) FROM sidikjari INNER JOIN dokter INNER JOIN pegawai ON sidikjari.id = pegawai.id  AND dokter.kd_dokter = pegawai.nik  WHERE dokter.kd_dokter = ?", kodedokter);
        param.put("finger", "Dikeluarkan di " + akses.getnamars() + ", Kabupaten/Kota " + akses.getkabupatenrs() + "\nDitandatangani secara elektronik oleh " + namadokter + "\nID " + (finger.equals("") ? kodedokter : finger) + "\n" + tanggal);
//        Valid.MyReportqrypdfKlaim("rptLaporanProgramKFR.jasper", "report", "5LAPPROGKFR",
//                "SELECT\n"
//                + "	laporan_program_kfr.no_rawat,\n"
//                + "	laporan_program_kfr.no_rkm_medis,\n"
//                + "	laporan_program_kfr.tanggal,\n"
//                + "	laporan_program_kfr.diagnosa,\n"
//                + "	laporan_program_kfr.tkm,\n"
//                + "	laporan_program_kfr.saran,\n"
//                + "	laporan_program_kfr.nik,\n"
//                + "	laporan_program_kfr.kd_dokter,\n"
//                + "	pasien.jk,\n"
//                + "	pasien.nm_pasien,\n"
//                + "	pasien.tmp_lahir,\n"
//                + "	pasien.tgl_lahir,\n"
//                + "	pasien.umur,\n"
//                + "	pasien.alamat,\n"
//                + "	pasien.no_ktp,\n"
//                + "	pegawai.nama,\n"
//                + "	dokter.nm_dokter \n"
//                + "FROM\n"
//                + "	laporan_program_kfr\n"
//                + "	INNER JOIN pasien ON laporan_program_kfr.no_rkm_medis = pasien.no_rkm_medis\n"
//                + "	INNER JOIN pegawai ON laporan_program_kfr.nik = pegawai.nik\n"
//                + "	INNER JOIN dokter ON laporan_program_kfr.kd_dokter = dokter.kd_dokter \n"
//                + "WHERE\n"
//                + "	laporan_program_kfr.no_rawat ='" + norawat + "' and laporan_program_kfr.tanggal ='" + tanggal + "'", param, "hasilkompilasiklaim", norawatuntuknamafile);

    }

    private void JKKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JKKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_JKKeyPressed

    private void KdDokActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_KdDokActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_KdDokActionPerformed

    private void KdDokKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdDokKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", TDokter, KdDok.getText());
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            btnDokter1ActionPerformed(null);
        } else {
            //Valid.pindah(evt, rujukke, PKartu);
        }
    }//GEN-LAST:event_KdDokKeyPressed

    private void TDokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TDokterKeyPressed
        //Valid.pindah(evt,TKd,TSpek);
    }//GEN-LAST:event_TDokterKeyPressed

    private void btnDokter1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDokter1ActionPerformed
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setVisible(true);
    }//GEN-LAST:event_btnDokter1ActionPerformed

    private void umurKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_umurKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_umurKeyPressed

    private void OKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_OKeyPressed
        //Valid.pindah2(evt, DiagnosisMedis, kesimpulan);
    }//GEN-LAST:event_OKeyPressed

    private void SKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_SKeyPressed

    private void AKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_AKeyPressed

    private void JKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_JKActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_JKActionPerformed

    private void goalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_goalKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_goalKeyPressed

    private void tindakanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tindakanKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tindakanKeyPressed

    private void edukasiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_edukasiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_edukasiKeyPressed

    private void frekuensiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_frekuensiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_frekuensiKeyPressed

    private void rencanaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_rencanaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_rencanaKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMFormAssesment dialog = new RMFormAssesment(new javax.swing.JFrame(), true);
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
    private widget.TextArea A;
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.CekBox ChkInput;
    private widget.CekBox ChkKejadian;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.ComboBox Detik;
    private widget.PanelBiasa FormInput;
    private widget.TextBox JK;
    private widget.ComboBox Jam;
    private widget.TextBox KdDok;
    private widget.Label LCount;
    private widget.ComboBox Menit;
    private javax.swing.JMenuItem MnUjiFungsi;
    private widget.TextArea O;
    private javax.swing.JPanel PanelInput;
    private widget.TextArea S;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.TextBox TDokter;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.Tanggal Tanggal;
    private widget.TextBox TglLahir;
    private widget.Button btnDokter1;
    private widget.TextBox edukasi;
    private widget.TextBox frekuensi;
    private widget.TextBox goal;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel10;
    private widget.Label jLabel16;
    private widget.Label jLabel19;
    private widget.Label jLabel21;
    private widget.Label jLabel22;
    private widget.Label jLabel23;
    private widget.Label jLabel24;
    private widget.Label jLabel25;
    private widget.Label jLabel26;
    private widget.Label jLabel27;
    private widget.Label jLabel28;
    private widget.Label jLabel29;
    private widget.Label jLabel30;
    private widget.Label jLabel31;
    private widget.Label jLabel4;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.TextBox rencana;
    private widget.ScrollPane scrollPane2;
    private widget.ScrollPane scrollPane3;
    private widget.ScrollPane scrollPane6;
    private widget.ScrollPane scrollPane7;
    private widget.Table tbObat;
    private widget.TextArea tindakan;
    private widget.TextBox umur;
    // End of variables declaration//GEN-END:variables

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            if (TCari.getText().toString().trim().equals("")) {
                ps = koneksi.prepareStatement(
                    "SELECT " +
                    "    reg_periksa.no_rawat, " +
                    "    reg_periksa.no_rkm_medis, " +
                    "    pasien.nm_pasien, " +
                    "    pasien.jk, " +
                    "    pasien.tgl_lahir, " +
                    "    CONCAT(reg_periksa.umurdaftar, ' ', reg_periksa.sttsumur) AS umur, " +
                    "    lembar_reassesment.tanggal, " +
                    "    lembar_reassesment.kd_dokter, " +
                    "    dokter.nm_dokter, " +
                    "    lembar_reassesment.subjective, " +
                    "    lembar_reassesment.objective, " +
                    "    lembar_reassesment.assesment, " +
                    "    lembar_reassesment.goal, " +
                    "    lembar_reassesment.tindakan, " +
                    "    lembar_reassesment.edukasi, " +
                    "    lembar_reassesment.frekuensi, " +
                    "    lembar_reassesment.rencana " +
                    "FROM reg_periksa " +
                    "INNER JOIN lembar_reassesment ON reg_periksa.no_rawat = lembar_reassesment.no_rawat " +
                    "INNER JOIN dokter ON lembar_reassesment.kd_dokter = dokter.kd_dokter " +
                    "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis " +
                    "WHERE lembar_reassesment.tanggal BETWEEN ? AND ?"
                );
            } else {
                ps = koneksi.prepareStatement(
                    "SELECT " +
                    "    reg_periksa.no_rawat, " +
                    "    reg_periksa.no_rkm_medis, " +
                    "    pasien.nm_pasien, " +
                    "    pasien.jk, " +
                    "    pasien.tgl_lahir, " +
                    "    CONCAT(reg_periksa.umurdaftar, ' ', reg_periksa.sttsumur) AS umur, " +
                    "    lembar_reassesment.tanggal, " +
                    "    lembar_reassesment.kd_dokter, " +
                    "    dokter.nm_dokter, " +
                    "    lembar_reassesment.subjective, " +
                    "    lembar_reassesment.objective, " +
                    "    lembar_reassesment.assesment, " +
                    "    lembar_reassesment.goal, " +
                    "    lembar_reassesment.tindakan, " +
                    "    lembar_reassesment.edukasi, " +
                    "    lembar_reassesment.frekuensi, " +
                    "    lembar_reassesment.rencana " +
                    "FROM " +
                    "    reg_periksa " +
                    "    INNER JOIN lembar_reassesment ON reg_periksa.no_rawat = lembar_reassesment.no_rawat " +
                    "    INNER JOIN dokter ON lembar_reassesment.kd_dokter = dokter.kd_dokter " +
                    "    INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis " +
                    "WHERE " +
                    "    lembar_reassesment.tanggal BETWEEN ? AND ? " +
                    "    AND (lembar_reassesment.no_rawat LIKE ? " +
                    "         OR pasien.nm_pasien LIKE ? " +
                    "         OR pasien.no_rkm_medis LIKE ?) "
                );

            }

            try {
                if (TCari.getText().toString().trim().equals("")) {

                    ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                    ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                } else {

                    ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                    ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                    ps.setString(3, "%" + TCari.getText() + "%");
                    ps.setString(4, "%" + TCari.getText() + "%");
                    ps.setString(5, "%" + TCari.getText() + "%");
                }

                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new String[]{
                        rs.getString("no_rawat"),
                        rs.getString("no_rkm_medis"),
                        rs.getString("nm_pasien"),
                        rs.getString("jk"),
                        rs.getString("tgl_lahir"),
                        rs.getString("umur"),
                        rs.getString("tanggal"),
                        rs.getString("kd_dokter"),
                        rs.getString("nm_dokter"),
                        rs.getString("subjective"),
                        rs.getString("objective"),
                        rs.getString("assesment"),
                        rs.getString("goal"),
                        rs.getString("tindakan"),
                        rs.getString("edukasi"),
                        rs.getString("frekuensi"),
                        rs.getString("rencana")
                    });

                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (SQLException e) {
            System.out.println("Notifikasi : " + e);
        }
        LCount.setText("" + tabMode.getRowCount());
    }

    public void emptTeks() {
        KdDok.setText("");
        TDokter.setText("");
        TNoRw.setText("");
        TNoRM.setText("");
        TPasien.setText("");
        JK.setText("");
        TglLahir.setText("");
        umur.setText("");
        S.setText("");
        O.setText("");
        A.setText("");
        goal.setText("");
        tindakan.setText("");
        edukasi.setText("");
        frekuensi.setText("");
        rencana.setText("");
        TNoRw.requestFocus();
    }

    private void getData() {
        if (tbObat.getSelectedRow() != -1) {
           TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 2).toString());
            JK.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 3).toString());
            // Jika ada kolom lahir & umur:
            TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 4).toString());
            umur.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString());

            Valid.SetTgl(Tanggal, tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString());
            Jam.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString().substring(11, 13));
            Menit.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString().substring(14, 16));
            Detik.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString().substring(17, 19));
            KdDok.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 7).toString());
            TDokter.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 8).toString());

            S.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 9).toString());
            O.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 10).toString());
            A.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 11).toString());
            goal.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 12).toString());
            tindakan.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 13).toString());
            edukasi.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 14).toString());
            frekuensi.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 15).toString());
            rencana.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 16).toString());
        }
    }
    
    private void isRawat() {
        Sequel.cariIsi("select reg_periksa.no_rkm_medis from reg_periksa where reg_periksa.no_rawat='" + TNoRw.getText() + "' ", TNoRM);
    }

    private void isPasien() {
        Sequel.cariIsi("select pasien.nm_pasien from pasien where pasien.no_rkm_medis='" + TNoRM.getText() + "' ", TPasien);
        Sequel.cariIsi("select pasien.jk from pasien where pasien.no_rkm_medis='" + TNoRM.getText() + "' ", JK);
        Sequel.cariIsi("select pasien.umur from pasien where pasien.no_rkm_medis='" + TNoRM.getText() + "' ", umur);
        Sequel.cariIsi("select date_format(pasien.tgl_lahir,'%d-%m-%Y') from pasien where pasien.no_rkm_medis=? ", TglLahir, TNoRM.getText());
    }

    public void setNoRm(String norwt, Date tgl1, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        S.setText(Sequel.cariIsi("select keluhan from pemeriksaan_ralan where no_rawat ='" + norwt + "' AND nip= '" +akses.getkode()+ "' "));
        O.setText(Sequel.cariIsi("select pemeriksaan from pemeriksaan_ralan where no_rawat ='" + norwt + "' AND nip= '" +akses.getkode()+ "' "));
        A.setText(Sequel.cariIsi("select penilaian from pemeriksaan_ralan where no_rawat ='" + norwt + "' AND nip= '" +akses.getkode()+ "' "));
        isRawat();
        isPasien();
        DTPCari1.setDate(tgl1);
        DTPCari2.setDate(tgl2);
        ChkInput.setSelected(true);
        isForm();
        isCek();
    }

    private void isForm() {
        if (ChkInput.isSelected() == true) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 300));
            FormInput.setVisible(true);
            ChkInput.setVisible(true);
        } else if (ChkInput.isSelected() == false) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 20));
            FormInput.setVisible(false);
            ChkInput.setVisible(true);
        }
    }

    public void isCek() {
        BtnSimpan.setEnabled(akses.getuji_fungsi_kfr());
        BtnHapus.setEnabled(akses.getuji_fungsi_kfr());
        BtnEdit.setEnabled(akses.getuji_fungsi_kfr());
        BtnPrint.setEnabled(akses.getuji_fungsi_kfr());

        if (akses.getjml2() >= 1) {
            KdDok.setEditable(false);
            KdDok.setText(akses.getkode());
            Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter=?", TDokter, KdDok.getText());
//            nip.setText("-");
            if (TDokter.getText().equals("")) {
                KdDok.setText("");
                JOptionPane.showMessageDialog(rootPane, "User login bukan dokter...!!");
            }else{
                btnDokter1.setEnabled(false);
            }
        }

        tampil();
        BtnCariActionPerformed(null);
    }

    private void jam() {
        ActionListener taskPerformer = new ActionListener() {
            private int nilai_jam;
            private int nilai_menit;
            private int nilai_detik;

            public void actionPerformed(ActionEvent e) {
                String nol_jam = "";
                String nol_menit = "";
                String nol_detik = "";

                Date now = Calendar.getInstance().getTime();

                // Mengambil nilaj JAM, MENIT, dan DETIK Sekarang
                if (ChkKejadian.isSelected() == true) {
                    nilai_jam = now.getHours();
                    nilai_menit = now.getMinutes();
                    nilai_detik = now.getSeconds();
                    
                    Tanggal.setDate(now);
                } else if (ChkKejadian.isSelected() == false) {
                    nilai_jam = Jam.getSelectedIndex();
                    nilai_menit = Menit.getSelectedIndex();
                    nilai_detik = Detik.getSelectedIndex();
                }

                // Jika nilai JAM lebih kecil dari 10 (hanya 1 digit)
                if (nilai_jam <= 9) {
                    // Tambahkan "0" didepannya
                    nol_jam = "0";
                }
                // Jika nilai MENIT lebih kecil dari 10 (hanya 1 digit)
                if (nilai_menit <= 9) {
                    // Tambahkan "0" didepannya
                    nol_menit = "0";
                }
                // Jika nilai DETIK lebih kecil dari 10 (hanya 1 digit)
                if (nilai_detik <= 9) {
                    // Tambahkan "0" didepannya
                    nol_detik = "0";
                }
                // Membuat String JAM, MENIT, DETIK
                String jam = nol_jam + Integer.toString(nilai_jam);
                String menit = nol_menit + Integer.toString(nilai_menit);
                String detik = nol_detik + Integer.toString(nilai_detik);
                // Menampilkan pada Layar
                //tampil_jam.setText("  " + jam + " : " + menit + " : " + detik + "  ");
                Jam.setSelectedItem(jam);
                Menit.setSelectedItem(menit);
                Detik.setSelectedItem(detik);
            }
        };
        // Timer
        new Timer(1000, taskPerformer).start();
    }

    private void ganti() {
        Sequel.mengedit("lembar_reassesment", " no_rawat=?", "subjective=?,objective=?,assesment=?,goal=?,tindakan=?,edukasi=?,frekuensi=?,rencana=?,tanggal=?,kd_dokter=?", 11, new String[]{
            S.getText(), O.getText(), A.getText(), goal.getText(), tindakan.getText(), edukasi.getText(), frekuensi.getText(), rencana.getText(),
            Valid.SetTgl(Tanggal.getSelectedItem() + "") + " " + Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(),KdDok.getText(),
            TNoRw.getText()
        });
        
        tampil();
        emptTeks();
    }

    private void hapus() {
        if (Sequel.queryu2tf("delete from lembar_reassesment where  no_rawat=?", 1, new String[]{
            tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString().trim()
        }) == true) {
            JOptionPane.showMessageDialog(rootPane, "Berhasil Menghapus..!!");
            tampil();
            emptTeks();
        } else {
            JOptionPane.showMessageDialog(rootPane, "Gagal menghapus..!!");
        }
    }

    public void ProgramKFRPdf(String norawat, String norm) {

        Map<String, Object> param = new HashMap<>();
        param.put("namars", akses.getnamars());
        param.put("alamatrs", akses.getalamatrs());
        param.put("kotars", akses.getkabupatenrs());
        param.put("propinsirs", akses.getpropinsirs());
        param.put("kontakrs", akses.getkontakrs());
        param.put("emailrs", akses.getemailrs());
        param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
//        Valid.ReportKompilasiBerkas("rptProgramKRF.jasper", "report", "::[ Form Program KFR ]::",
//                "SELECT\n"
//                + "	pasien.no_rkm_medis, \n"
//                + "	pasien.nm_pasien, \n"
//                + "	pasien.umur, \n"
//                + "	pasien.jk, \n"
//                + "	pasien.tgl_lahir, \n"
//                + "	program_kfr.tanggal, \n"
//                + "	program_kfr.diagnosa, \n"
//                + "	program_kfr.perm_terapi, \n"
//                + "	program_kfr.program, \n"
//                + "	petugas.nip, \n"
//                + "	petugas.nama, \n"
//                + "	dokter.kd_dokter, \n"
//                + "	dokter.nm_dokter\n"
//                + "FROM\n"
//                + "	pasien\n"
//                + "	INNER JOIN\n"
//                + "	program_kfr\n"
//                + "	ON \n"
//                + "		pasien.no_rkm_medis = program_kfr.no_rkm_medis\n"
//                + "	INNER JOIN\n"
//                + "	petugas\n"
//                + "	ON \n"
//                + "		program_kfr.nik = petugas.nip\n"
//                + "	INNER JOIN\n"
//                + "	dokter\n"
//                + "	ON \n"
//                + "		program_kfr.kd_dokter = dokter.kd_dokter "
//                + " where pasien.no_rkm_medis = '" + norm + "' "
//                + " order by program_kfr.tanggal", param, norawat, norm, "PROGRAMKFR");

    }
}

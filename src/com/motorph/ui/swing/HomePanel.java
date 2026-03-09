/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.motorph.ui.swing;

import com.motorph.repository.csv.DataPaths;
import com.motorph.service.strategy.PayDeductionStrategy;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;

/**
 * Home dashboard details panel.
 *
 * @author ACER
 */
public class HomePanel extends javax.swing.JPanel {

    private JTextPane txtDetails;
    private JTextPane txtGovGuide;
    private JScrollPane scrollDetails;
    private JScrollPane scrollGovGuide;
    private JComboBox<String> cbGuideType;
    private JButton btnGuideInfo;
    private double referenceSalary;
    private double referenceTaxableIncome;

    public HomePanel() {
        initComponents();
        initCustomComponents();
    }

    public HomePanel(com.motorph.domain.models.User currentUser) {
        this();
    }

    private void initCustomComponents() {
        txtDetails = new JTextPane();
        txtGovGuide = new JTextPane();
        scrollDetails = new JScrollPane();
        scrollGovGuide = new JScrollPane();
        cbGuideType = new JComboBox<>(new String[]{"SSS", "PhilHealth", "Pag-IBIG", "Tax"});
        btnGuideInfo = new JButton("i");

        removeAll();
        setLayout(new BorderLayout());

        txtDetails.setContentType("text/html");
        txtDetails.setEditable(false);

        txtGovGuide.setContentType("text/html");
        txtGovGuide.setEditable(false);

        scrollDetails.setViewportView(txtDetails);
        scrollGovGuide.setViewportView(txtGovGuide);

        JPanel guideSelectorPanel = new JPanel(new BorderLayout(6, 0));
        guideSelectorPanel.add(cbGuideType, BorderLayout.CENTER);
        btnGuideInfo.setToolTipText("View the full government deduction table");
        btnGuideInfo.setFocusable(false);
        btnGuideInfo.setMargin(new java.awt.Insets(2, 8, 2, 8));
        guideSelectorPanel.add(btnGuideInfo, BorderLayout.EAST);

        JPanel rightTop = new JPanel(new BorderLayout(0, 6));
        rightTop.add(guideSelectorPanel, BorderLayout.NORTH);
        rightTop.add(scrollGovGuide, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollDetails, rightTop);
        splitPane.setResizeWeight(0.68);
        splitPane.setDividerLocation(0.68);
        splitPane.setContinuousLayout(true);

        add(splitPane, BorderLayout.CENTER);

        cbGuideType.addActionListener(e -> refreshGovGuide());
        btnGuideInfo.addActionListener(e -> showFullGuideDialog());
        refreshGovGuide();

        revalidate();
        repaint();
    }

    public void setDetailsHtml(String html) {
        if (txtDetails != null) {
            txtDetails.setText(html != null ? html : "");
            txtDetails.setCaretPosition(0);
        }
    }

    public void setDeductionContext(double monthlySalary, double taxableIncome) {
        this.referenceSalary = monthlySalary;
        this.referenceTaxableIncome = taxableIncome;
        refreshGovGuide();
    }

    private void showFullGuideDialog() {
        String selected = String.valueOf(cbGuideType.getSelectedItem());
        JTextPane pane = new JTextPane();
        pane.setContentType("text/html");
        pane.setEditable(false);
        pane.setText(buildFullGuideHtml(selected));
        pane.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(pane);
        scroll.setPreferredSize(new Dimension(860, 520));

        JDialog dialog = new JDialog(javax.swing.SwingUtilities.getWindowAncestor(this), selected + " Full Table", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(scroll, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String buildFullGuideHtml(String selected) {
        String path;
        String title;
        String note;
        switch (selected) {
            case "PhilHealth":
                path = DataPaths.GOV_PHILHEALTH_CSV;
                title = "PhilHealth Full Table";
                note = "Full salary brackets and rates from the PhilHealth reference table.";
                break;
            case "Pag-IBIG":
                path = DataPaths.GOV_PAGIBIG_CSV;
                title = "Pag-IBIG Full Table";
                note = "Full employee-share rules used for Pag-IBIG computation.";
                break;
            case "Tax":
                path = DataPaths.GOV_TAX_CSV;
                title = "Withholding Tax Full Table";
                note = "Full taxable-income brackets and rules from the withholding tax reference table.";
                break;
            default:
                path = DataPaths.GOV_SSS_CSV;
                title = "SSS Full Table";
                note = "Full salary brackets and contribution values from the SSS reference table.";
                break;
        }
        return buildCsvTableHtml(path, title, note);
    }

    private String buildCsvTableHtml(String path, String title, String note) {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                rows.add(line.split(",", -1));
            }
        } catch (IOException e) {
            return "<html><body style='font-family:Segoe UI; font-size:12px; margin:12px;'><b>Unable to read table.</b></body></html>";
        }
        if (rows.isEmpty()) {
            return "<html><body style='font-family:Segoe UI; font-size:12px; margin:12px;'><b>No table data found.</b></body></html>";
        }

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:Segoe UI; font-size:12px; margin:12px;'>");
        html.append("<div style='font-size:14pt; font-weight:bold; margin-bottom:6px;'>").append(esc(title)).append("</div>");
        html.append("<div style='margin-bottom:10px;'>").append(esc(note)).append("</div>");
        html.append("<table style='width:100%; border-collapse:collapse; font-size:12px;' border='1' cellpadding='6'>");
        String[] header = rows.get(0);
        html.append("<tr style='font-weight:bold; background:#f2f2f2;'>");
        for (String h : header) {
            html.append("<td>").append(esc(h)).append("</td>");
        }
        html.append("</tr>");
        for (int i = 1; i < rows.size(); i++) {
            html.append("<tr>");
            for (String cell : rows.get(i)) {
                html.append("<td>").append(esc(cell)).append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</table></body></html>");
        return html.toString();
    }

    private void refreshGovGuide() {
        if (txtGovGuide == null || cbGuideType == null) {
            return;
        }

        String selected = String.valueOf(cbGuideType.getSelectedItem());
        if ("PhilHealth".equals(selected)) {
            txtGovGuide.setText(buildPhilHealthHtml(referenceSalary));
        } else if ("Pag-IBIG".equals(selected)) {
            txtGovGuide.setText(buildPagibigHtml(referenceSalary));
        } else if ("Tax".equals(selected)) {
            txtGovGuide.setText(buildTaxHtml(referenceTaxableIncome));
        } else {
            txtGovGuide.setText(buildSssHtml(referenceSalary));
        }
        txtGovGuide.setCaretPosition(0);
    }

    private String buildSssHtml(double monthlySalary) {
        String matched = "<tr><td colspan='4'>No matching bracket found.</td></tr>";
        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.GOV_SSS_CSV))) {
            String header = br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] d = line.split(",", -1);
                if (d.length < 6) continue;
                double min = parse(d[1]);
                double max = parse(d[2]);
                if (monthlySalary >= min && monthlySalary <= max) {
                    double ee = parse(d[5]);
                    matched = "<tr><td>Monthly Salary</td><td>₱" + money(monthlySalary) + "</td><td>Bracket</td><td>₱" + money(min) + " - ₱" + money(max) + "</td></tr>"
                            + "<tr><td>Employee Share</td><td>₱" + money(ee) + "</td><td>Source</td><td>Total EE Contribution</td></tr>";
                    break;
                }
            }
        } catch (IOException e) {
            matched = "<tr><td colspan='4'>Unable to read SSS table.</td></tr>";
        }
        return wrapGuideHtml("SSS Contribution Guide", "Monthly salary is matched against the SSS contribution bracket table.", matched);
    }

    private String buildPhilHealthHtml(double monthlySalary) {
        String matched = "<tr><td colspan='4'>No matching bracket found.</td></tr>";
        PayDeductionStrategy strategy = new PayDeductionStrategy();
        double share = strategy.calculatePhilHealth(monthlySalary);
        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.GOV_PHILHEALTH_CSV))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] d = line.split(",", -1);
                if (d.length < 7) continue;
                double min = parse(d[1]);
                double max = parse(d[2]);
                if (monthlySalary >= min && monthlySalary <= max) {
                    matched = "<tr><td>Monthly Salary</td><td>₱" + money(monthlySalary) + "</td><td>Bracket</td><td>₱" + money(min) + " - ₱" + money(max) + "</td></tr>"
                            + "<tr><td>Employee Share</td><td>₱" + money(share) + "</td><td>Rule</td><td>Based on employee share / table cap</td></tr>";
                    break;
                }
            }
        } catch (IOException e) {
            matched = "<tr><td colspan='4'>Unable to read PhilHealth table.</td></tr>";
        }
        return wrapGuideHtml("PhilHealth Contribution Guide", "PhilHealth uses salary brackets, premium rate, and minimum or maximum caps.", matched);
    }

    private String buildPagibigHtml(double monthlySalary) {
        PayDeductionStrategy strategy = new PayDeductionStrategy();
        double share = strategy.calculatePagibig(monthlySalary);
        double rate = monthlySalary <= 1500 ? 0.01 : 0.02;
        String matched = "<tr><td>Monthly Salary</td><td>₱" + money(monthlySalary) + "</td><td>Employee Rate</td><td>" + percent(rate) + "</td></tr>"
                + "<tr><td>Employee Share</td><td>₱" + money(share) + "</td><td>Cap Applied</td><td>₱100.00 maximum contribution</td></tr>";
        return wrapGuideHtml("Pag-IBIG Contribution Guide", "Pag-IBIG uses 1% for salaries up to ₱1,500.00 and 2% above that, capped at ₱100.00.", matched);
    }

    private String buildTaxHtml(double taxableIncome) {
        String matched = "<tr><td colspan='4'>No matching bracket found.</td></tr>";
        PayDeductionStrategy strategy = new PayDeductionStrategy();
        double tax = strategy.calculateTax(taxableIncome);
        try (BufferedReader br = new BufferedReader(new FileReader(DataPaths.GOV_TAX_CSV))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] d = line.split(",", -1);
                if (d.length < 5) continue;
                double min = parse(d[1]);
                double max = parse(d[2]);
                if (taxableIncome >= min && taxableIncome <= max) {
                    matched = "<tr><td>Taxable Income</td><td>₱" + money(taxableIncome) + "</td><td>Bracket</td><td>₱" + money(min) + " - ₱" + money(max) + "</td></tr>"
                            + "<tr><td>Withholding Tax</td><td>₱" + money(tax) + "</td><td>Rule</td><td>Base tax + excess percent on bracket</td></tr>";
                    break;
                }
            }
        } catch (IOException e) {
            matched = "<tr><td colspan='4'>Unable to read tax table.</td></tr>";
        }
        return wrapGuideHtml("Withholding Tax Guide", "Tax is computed using the taxable-income bracket from the government tax table.", matched);
    }

    private String wrapGuideHtml(String title, String note, String rows) {
        return "<html><body style='font-family:Segoe UI; font-size:12px; margin:8px;'>"
                + "<div style='font-size:12pt; font-weight:bold; margin-bottom:6px;'>" + esc(title) + "</div>"
                + "<div style='margin-bottom:8px;'>" + esc(note) + "</div>"
                + "<table style='width:100%; border-collapse:collapse; font-size:12px;' border='1' cellpadding='6'>"
                + "<tr style='font-weight:bold; background-color:#f2f2f2;'><td>Item</td><td>Value</td><td>Basis</td><td>Details</td></tr>"
                + rows
                + "</table></body></html>";
    }

    private String money(double value) {
        return String.format("%,.2f", value);
    }

    private String percent(double value) {
        return String.format("%.0f%%", value * 100.0);
    }

    private double parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(raw.trim());
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 651, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 531, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}

package com.code.mycode;

import static javax.swing.KeyStroke.getKeyStroke;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import com.code.mycode.entities.Entity.AnalysisContext;
import com.code.mycode.entities.Program;
import com.code.mycode.syntax.Parser;
import com.code.translators.MyCodeToJavaScriptTranslator;
import com.code.util.Log;

/**
 * A simple GUI application for viewing the different things the MyCode compiler can do.
 *
 * The application has two panes.  The left is a simple text editor in which one can edit a MyCode
 * program, load one from the file system, and save to the file system.  The right shows a view of
 * the program in response to a user selection action.  The user can choose to see
 * <ul>
 *   <li>The abstract syntax tree
 *   <li>The semantic graph
 *   <li>The optimzed semantic graph
 *   <li>A translation to JavaScript
 * </ul>
 */
@SuppressWarnings("serial")
public class Viewer extends JFrame {

    private JTextArea source = new JTextArea(30, 60);
    private JTextArea view = new JTextArea(30, 80) {
        public void setText(String text) {
            super.setText(text);
            this.setCaretPosition(0);
        }
    };
    private JScrollPane sourcePane = new JScrollPane();
    private JScrollPane viewPane = new JScrollPane();
    private StringWriter errors = new StringWriter();
    private Log log = new Log("MyCode", new PrintWriter(errors));
    private File currentFile;
    private JFileChooser chooser = new JFileChooser(".");

    public Viewer() {
        JSplitPane splitPane = new JSplitPane();
        JMenuBar menuBar = new JMenuBar();

        Action newAction = new AbstractAction("New") {
            {
                putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
                putValue(Action.ACCELERATOR_KEY, getKeyStroke("control N"));
            }
            public void actionPerformed(ActionEvent e) {newFile();}
        };

        Action openAction = new AbstractAction("Open") {
            {
                putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
                putValue(Action.ACCELERATOR_KEY, getKeyStroke("control O"));
            }
            public void actionPerformed(ActionEvent e) {openFile();}
        };

        Action saveAction = new AbstractAction("Save") {
            {
                putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
                putValue(Action.ACCELERATOR_KEY, getKeyStroke("control S"));
            }
            public void actionPerformed(ActionEvent e) {saveFile();}
        };

        Action saveAsAction = new AbstractAction("Save As") {
            public void actionPerformed(ActionEvent e) {saveAsFile();}
        };

        new AbstractAction("Quit") {
            {
                putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Q);
                putValue(Action.ACCELERATOR_KEY, getKeyStroke("control Q"));
            }
            public void actionPerformed(ActionEvent e) {quit();}
        };

        Action syntaxAction = new AbstractAction("Analisis Sintactico") {
            public void actionPerformed(ActionEvent e) {viewSyntaxTree();}
        };

        Action semanticsAction = new AbstractAction("Analisis Semantico") {
            public void actionPerformed(ActionEvent e) {viewSemanticGraph();}
        };

        Action optimizeAction = new AbstractAction("Optimize") {
            public void actionPerformed(ActionEvent e) {viewOptimizedSemanticGraph();}
        };

        Action javaScriptAction = new AbstractAction("JavaScript") {
            public void actionPerformed(ActionEvent e) {viewJavaScript();}
        };

        //menuBar.add(new JButton(newAction));
        //menuBar.add(new JButton(openAction));
       // menuBar.add(new JButton(saveAction));
       // menuBar.add(new JButton(saveAsAction));
        menuBar.add(new JButton(syntaxAction));
        menuBar.add(new JButton(semanticsAction));
        //menuBar.add(new JButton(optimizeAction));
        //menuBar.add(new JButton(javaScriptAction));
        setJMenuBar(menuBar);

        source.setFont(new Font("Monospaced", 0, 16));
        sourcePane.setViewportView(source);
        splitPane.setLeftComponent(sourcePane);
        view.setEditable(false);
        view.setFont(new Font("Monospaced", 0, 16));
        viewPane.setViewportView(view);
        splitPane.setRightComponent(viewPane);
        splitPane.setDividerLocation(480);
        getContentPane().add(splitPane, BorderLayout.CENTER);

        setTitle("Mi compilador");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 712);
        
        source.setText(
           "struct T {\n" +
            "   int a;\n" +
            "   int b;\n" +
            "   string a;\n" +
            "}\n" +
            "\n" +
            "string[] colores = new string[]{\n" +
            " \"azul\", \"verde\", \"rojo\", false\n" +
            "};\n" +
            "\n" +
            "int x = \"hola\";\n" +
            "\n" +
            "void f(int x){\n" +
            " return 10/true;\n" +
            " break;\n" +
            "}\n" +
            "\n" +
            "while 7 {\n" +
            " print f(3);\n" +
            "}\n" +
            "\n" +
            "f(null, null);\n" +
            "return;"
        );
    }

    private void newFile() {
        currentFile = null;
        source.setText("");
    }

    private void openFile() {
        chooser.showOpenDialog(this);
        currentFile = chooser.getSelectedFile();
        if (currentFile == null) return;

        StringBuffer buffer = new StringBuffer();
        try {
            BufferedReader in = new BufferedReader(
                new FileReader(currentFile.getCanonicalPath()));
            String line;
            while ((line = in.readLine()) != null) {
                buffer.append(line).append("\n");
            }
            in.close();
        } catch (IOException ignored) {
        }
        source.setText(buffer.toString());
    }

    private void saveFile() {
        if (currentFile == null) {
            view.setText("Nothing to save");
            return;
        }
        try {
            BufferedWriter out = new BufferedWriter(
                new FileWriter(currentFile.getCanonicalPath()));
            out.write(source.getText());
            out.close();
            view.setText("File saved");
        } catch (IOException e) {
            view.setText("File not saved: " + e.getMessage());
        }
    }

    private void saveAsFile() {
        chooser.showSaveDialog(this);
        currentFile = chooser.getSelectedFile();
        if (currentFile != null) {
            saveFile();
        }
    }

    private void quit() {
        System.exit(0);
    }

    private void viewSyntaxTree() {
        try {
            Program program = parse();
            if (log.getErrorCount() > 0) {
                displayErrorOutput();
            } else {
                StringWriter writer = new StringWriter();
                program.printSyntaxTree("", "", new PrintWriter(writer));
                displaySuccessfulOutput(writer);
            }
        } catch (Exception e) {
            displayExceptionOutput(e);
        }
    }

    private void viewSemanticGraph() {
        try {
            Program program = analyze();
            if (log.getErrorCount() > 0) {
                displayErrorOutput();
            } else {
                StringWriter writer = new StringWriter();
                program.printEntities(new PrintWriter(writer));
                displaySuccessfulOutput(writer);
            }
        } catch (Exception e) {
            displayExceptionOutput(e);
        }
    }

    private void viewOptimizedSemanticGraph() {
        try {
            Program program = optimize();
            if (log.getErrorCount() > 0) {
                displayErrorOutput();
            } else {
                StringWriter writer = new StringWriter();
                program.printEntities(new PrintWriter(writer));
                displaySuccessfulOutput(writer);
            }
        } catch (Exception e) {
            displayExceptionOutput(e);
        }
    }

    private void viewJavaScript() {
        try {
            StringWriter writer = toJavaScript();
            if (log.getErrorCount() > 0) {
                displayErrorOutput();
            } else {
                displaySuccessfulOutput(writer);
            }
        } catch (Exception e) {
            displayExceptionOutput(e);
        }
    }

    private Program parse() {
        log.clearErrors();
        errors.getBuffer().setLength(0);
        Reader reader = new StringReader(source.getText());
        return new Parser(reader).parse(reader, log);
    }

    private Program analyze() {
        Program program = parse();
        if (log.getErrorCount() > 0) return null;
        program.analyze(AnalysisContext.makeGlobalContext(log));
        return program;
    }

    private Program optimize() {
        Program program = analyze();
        if (log.getErrorCount() > 0) return null;
        program.optimize();
        return program;
    }

    private StringWriter toJavaScript() {
        Program program = optimize();
        if (log.getErrorCount() > 0) return null;
        StringWriter writer = new StringWriter();
        new MyCodeToJavaScriptTranslator().translateProgram(program, new PrintWriter(writer));
        return writer;
    }

    private void displaySuccessfulOutput(StringWriter writer) {
        view.setBackground(Color.GREEN);
        view.setText(writer.toString());
    }

    private void displayErrorOutput() {
        view.setBackground(Color.ORANGE);
        view.setText(errors.toString());
    }

    private void displayExceptionOutput(Exception e) {
        view.setBackground(Color.RED);
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        view.setText(writer.toString());
    }

    public static void main(String args[]) {
        new Viewer().setVisible(true);
    }
}

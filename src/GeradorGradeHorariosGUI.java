import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GeradorGradeHorariosGUI extends JFrame {

    public GeradorGradeHorariosGUI() {
        setTitle("Grade de Horários - 50 Linhas com 100 Colunas");
        setSize(1600, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        DefaultTableModel modelo = new DefaultTableModel(50, 100);
        JTable tabela = new JTable(modelo) {
            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                return new MultilineTableCellRenderer();
            }
        };

        for (int i = 0; i < tabela.getColumnCount(); i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth(900); // largura ajustada
        }

        tabela.setRowHeight(30); // altura aumentada para visualização

        JScrollPane scrollPane = new JScrollPane(tabela);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        List<String> dias = Arrays.asList("Seg", "Ter", "Qua", "Qui", "Sex");
        List<String> horarios = Arrays.asList("1H", "2H", "3H", "4H");

        List<String> materias = Arrays.asList(
                "Matemática", "Português", "História", "Geografia", "Física",
                "Química", "Biologia", "Inglês", "Artes", "Educação Física",
                "Filosofia", "Sociologia", "Literatura", "Redação", "Informática",
                "Programação", "Banco de Dados", "Algoritmos", "Empreendedorismo", "Robótica"
        );

        List<String> professores = Arrays.asList(
                "Ana", "Bruno", "Carlos", "Daniela", "Eduardo",
                "Fernanda", "Gustavo", "Helena", "Isabela", "João"
        );

        Random rand = new Random();
        int coluna = 0;

        for (int i = 0; i < 100; i++) {
            if (i < 20) {
                modelo.setValueAt("1º Período", 0, i);
            } else if (i < 40) {
                modelo.setValueAt("2º Período", 0, i);
            } else if (i < 60) {
                modelo.setValueAt("3º Período", 0, i);
            } else if (i < 80) {
                modelo.setValueAt("4º Período", 0, i);
            } else {
                modelo.setValueAt("5º Período", 0, i);
            }
        }

        coluna = 0;
        for (int periodo = 1; periodo <= 5; periodo++) {
            for (String dia : dias) {
                for (String horario : horarios) {
                    modelo.setValueAt(dia + " - " + horario, 1, coluna);
                    coluna++;
                }
            }
        }

        for (int linha = 2; linha < 50; linha++) {
            coluna = 0;
            for (String dia : dias) {
                for (int periodo = 1; periodo <= 5; periodo++) {
                    String professor = professores.get(rand.nextInt(professores.size()));
                    String materia = materias.get(rand.nextInt(materias.size()));
                    for (String horario : horarios) {
                        modelo.setValueAt(professor + "\n" + materia, linha, coluna);
                        coluna++;
                    }
                }
            }
        }

        Set<Integer> colunasComConflito = new HashSet<>();
        Map<Integer, Integer> conflitosPorLinha = new HashMap<>();

        for (int linha = 2; linha < 50; linha++) {
            for (int i = 0; i < dias.size(); i++) {
                for (int j = 0; j < horarios.size(); j++) {
                    Map<String, List<Integer>> professorPorPeriodo = new HashMap<>();
                    for (int periodo = 0; periodo < 5; periodo++) {
                        int index = i * 20 + j + periodo * 4;
                        String conteudo = (String) modelo.getValueAt(linha, index);
                        if (conteudo == null || !conteudo.contains("\n")) continue;
                        String professor = conteudo.split("\n")[0];
                        professorPorPeriodo.computeIfAbsent(professor, k -> new ArrayList<>()).add(index);
                    }
                    for (List<Integer> indices : professorPorPeriodo.values()) {
                        if (indices.size() > 1) {
                            colunasComConflito.addAll(indices);
                            conflitosPorLinha.put(linha, conflitosPorLinha.getOrDefault(linha, 0) + 1);
                        }
                    }
                }
            }
        }

        List<Map.Entry<Integer, Integer>> linhasOrdenadas = new ArrayList<>(conflitosPorLinha.entrySet());
        linhasOrdenadas.sort(Comparator.comparingInt(Map.Entry::getValue));

        System.out.println("Linhas ordenadas por quantidade de conflitos:");
        for (Map.Entry<Integer, Integer> entry : linhasOrdenadas) {
            System.out.println("Linha " + (entry.getKey() + 1) + " - Conflitos: " + entry.getValue());
        }

        DefaultTableModel modeloOrdenado = new DefaultTableModel(50, 100);

        // Copia os cabeçalhos e horários
        for (int col = 0; col < 100; col++) {
            modeloOrdenado.setValueAt(modelo.getValueAt(0, col), 0, col);
            modeloOrdenado.setValueAt(modelo.getValueAt(1, col), 1, col);
        }

        int novaLinha = 2;
        for (Map.Entry<Integer, Integer> entry : linhasOrdenadas) {
            int linhaOriginal = entry.getKey();
            for (int col = 0; col < 100; col++) {
                modeloOrdenado.setValueAt(modelo.getValueAt(linhaOriginal, col), novaLinha, col);
            }
            novaLinha++;
        }

        tabela.setModel(modeloOrdenado);

        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (colunasComConflito.contains(column) && row >= 2) {
                    c.setForeground(Color.RED);
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GeradorGradeHorariosGUI frame = new GeradorGradeHorariosGUI();
            frame.setVisible(true);
        });
    }
}

// Renderer que permite múltiplas linhas por célula
class MultilineTableCellRenderer extends JTextArea implements TableCellRenderer {
    public MultilineTableCellRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        setText(value == null ? "" : value.toString());
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }
        setFont(table.getFont());
        return this;
    }
}

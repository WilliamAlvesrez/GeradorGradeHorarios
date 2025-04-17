import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

public class GeradorGradeHorariosGUI extends JFrame {

    public GeradorGradeHorariosGUI() {
        setTitle("Grade de Horários");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] colunas = {"Dia", "Horário", "Período", "Matéria", "Professor"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0);
        JTable tabela = new JTable(modelo);

        List<String> dias = Arrays.asList("Segunda", "Terça", "Quarta", "Quinta", "Sexta");
        List<String> horarios = Arrays.asList("1H", "2H", "3H", "4H");

        // List<String> horarios = Collections.singletonList("19h–23h");

        List<String> materias = Arrays.asList(
            "Matemática", "Português", "História", "Geografia", "Física",
            "Química", "Biologia", "Inglês", "Artes", "Educação Física",
            "Filosofia", "Sociologia", "Literatura", "Redação", "Informática",
            "Programação", "Banco de Dados", "Algoritmos", "Empreendedorismo", "Educação Financeira",
            "Robótica", "Projetos Interdisciplinares", "Ética", "Cidadania", "Psicologia Educacional"
        );

        List<String> professores = Arrays.asList(
            "Ana Paula", "Bruno Souza", "Carlos Silva", "Daniela Lima", "Eduardo Martins",
            "Fernanda Rocha", "Gustavo Alves", "Helena Castro", "Isabela Nunes", "João Pedro"
        );

        int totalLinhas = 60;
        int totalPeriodos = 5;

        List<String> materiasDisponiveis = new ArrayList<>(materias);
        Collections.shuffle(materiasDisponiveis);

        Map<Integer, Set<String>> professoresPorPeriodo = new HashMap<>();
        for (int p = 1; p <= totalPeriodos; p++) {
            professoresPorPeriodo.put(p, new HashSet<>());
        }

        int linhasGeradas = 0;
        while (linhasGeradas < totalLinhas) {
            for (int periodo = 1; periodo <= totalPeriodos && linhasGeradas < totalLinhas; periodo++) {
                for (String dia : dias) {
                    if (linhasGeradas >= totalLinhas) break;

                    String horario = horarios.get(0);

                    if (materiasDisponiveis.isEmpty()) {
                        materiasDisponiveis = new ArrayList<>(materias);
                        Collections.shuffle(materiasDisponiveis);
                    }

                    String materia = materiasDisponiveis.remove(0);
                    Set<String> usados = professoresPorPeriodo.get(periodo);
                    List<String> candidatos = new ArrayList<>(professores);
                    Collections.shuffle(candidatos);

                    String professor = null;
                    for (String candidato : candidatos) {
                        if (!usados.contains(candidato)) {
                            professor = candidato;
                            break;
                        }
                    }

                    if (professor == null) {
                        professoresPorPeriodo.get(periodo).clear();
                        Collections.shuffle(candidatos);
                        professor = candidatos.get(0);
                    }

                    professoresPorPeriodo.get(periodo).add(professor);

                    modelo.addRow(new Object[]{dia, horario, periodo, materia, professor});
                    linhasGeradas++;
                }
            }
        }

        add(new JScrollPane(tabela), BorderLayout.CENTER);

        // ===== Avaliação de conflitos e destaque de linhas =====

        // 1) Avalia os conflitos (conta por dia, mas não usaremos diretamente esse mapa para colorir)
        Map<String, Integer> conflitos = avaliarConflitos(modelo);

        // Ordena e imprime os conflitos no terminal de Segunda a Sexta
        String[] diasSemana = {"Segunda", "Terça", "Quarta", "Quinta", "Sexta"};
        int totalConflitos = 0;
        for (String dia : diasSemana) {
            int qtdConflitos = conflitos.getOrDefault(dia, 0);
            System.out.println("Conflitos em " + dia + ": " + qtdConflitos);
            totalConflitos += qtdConflitos;
        }

        System.out.println("\nTotal de conflitos na grade: " + totalConflitos);

        // 2) Monta um mapa dia→professor→lista de índices de linha
        Map<String, Map<String, List<Integer>>> mapa = new HashMap<>();
        for (int i = 0; i < modelo.getRowCount(); i++) {
            String dia = modelo.getValueAt(i, 0).toString();
            String prof = modelo.getValueAt(i, 4).toString();
            mapa
              .computeIfAbsent(dia, d -> new HashMap<>())
              .computeIfAbsent(prof, p -> new ArrayList<>())
              .add(i);
        }

        // 3) Coleta em um Set os índices de todas as linhas com professor repetido no mesmo dia
        Set<Integer> linhasComConflito = new HashSet<>();
        for (Map<String, List<Integer>> profMap : mapa.values()) {
            for (List<Integer> idxs : profMap.values()) {
                if (idxs.size() > 1) {
                    linhasComConflito.addAll(idxs);
                }
            }
        }

        // 4) Renderer que pinta de vermelho apenas as linhas em conflito
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
                if (linhasComConflito.contains(row)) {
                    c.setForeground(Color.RED);
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
    }

    // Classe auxiliar para representar cada linha da grade
    static class Grade {
        String dia;
        int periodo;
        String professor;
        Grade(String dia, int periodo, String professor) {
            this.dia = dia;
            this.periodo = periodo;
            this.professor = professor;
        }
    }

    // Método que conta conflitos de professor por dia
    public static Map<String, Integer> avaliarConflitos(DefaultTableModel modelo) {
        List<Grade> gradeCompleta = new ArrayList<>();
        for (int i = 0; i < modelo.getRowCount(); i++) {
            String dia = modelo.getValueAt(i, 0).toString();
            int periodo = Integer.parseInt(modelo.getValueAt(i, 2).toString());
            String prof = modelo.getValueAt(i, 4).toString();
            gradeCompleta.add(new Grade(dia, periodo, prof));
        }

        Map<String, Integer> conflitosPorDia = new HashMap<>();
        String[] diasSemana = {"Segunda", "Terça", "Quarta", "Quinta", "Sexta"};
        for (String dia : diasSemana) {
            Map<String, Set<Integer>> mapaDia = new HashMap<>();
            for (Grade g : gradeCompleta) {
                if (g.dia.equals(dia)) {
                    mapaDia.putIfAbsent(g.professor, new HashSet<>());
                    mapaDia.get(g.professor).add(g.periodo);
                }
            }
            int cont = 0;
            for (Set<Integer> ps : mapaDia.values()) {
                if (ps.size() > 1) cont++;
            }
            conflitosPorDia.put(dia, cont);
        }
        return conflitosPorDia;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GeradorGradeHorariosGUI frame = new GeradorGradeHorariosGUI();
            frame.setVisible(true);
        });
    }
}
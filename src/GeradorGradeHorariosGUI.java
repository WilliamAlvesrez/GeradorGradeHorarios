import javax.swing.*;
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
        List<String> horarios = Collections.singletonList("19h–23h");

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
        
        //adiciona o numero de linhas para gerar a grade horária para 5 períodos

        int totalLinhas = 60;
        int totalPeriodos = 5;

        List<String> materiasDisponiveis = new ArrayList<>(materias);
        Collections.shuffle(materiasDisponiveis);

        // Mapeia professores já utilizados por período
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
                    String materia;

                    if (materiasDisponiveis.isEmpty()) {
                        materiasDisponiveis = new ArrayList<>(materias);
                        Collections.shuffle(materiasDisponiveis);
                    }

                    materia = materiasDisponiveis.remove(0);

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

                    // Se todos foram usados, reinicia o set do período
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
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GeradorGradeHorariosGUI frame = new GeradorGradeHorariosGUI();
            frame.setVisible(true);
        });
    }
}

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class LostLinkMatchingLogic {

    // Attribute weights
    private static final double DESCRIPTION_WEIGHT = 0.30;
    private static final double CATEGORY_WEIGHT = 0.15;
    private static final double BRAND_WEIGHT = 0.15;
    private static final double COLOUR_WEIGHT = 0.15;
    private static final double LOCATION_WEIGHT = 0.15;
    private static final double DATE_WEIGHT = 0.10;

    static class ItemReport {
        String description;
        String category;
        String brand;
        String colour;
        String location;
        LocalDate date;

        ItemReport(String description, String category, String brand,
                   String colour, String location, LocalDate date) {
            this.description = description;
            this.category = category;
            this.brand = brand;
            this.colour = colour;
            this.location = location;
            this.date = date;
        }
    }

    static class MatchResult {
        double percentage;
        List<String> reasons;

        MatchResult(double percentage, List<String> reasons) {
            this.percentage = percentage;
            this.reasons = reasons;
        }
    }

    public static MatchResult compare(ItemReport lost, ItemReport found) {

        double weightedTotal = 0.0;
        double weightsUsed = 0.0;
        List<String> reasons = new ArrayList<>();

        double[] result;

        result = addTextScore("Description", lost.description,
                found.description, DESCRIPTION_WEIGHT,
                weightedTotal, weightsUsed, reasons);

        weightedTotal = result[0];
        weightsUsed = result[1];

        result = addTextScore("Category", lost.category,
                found.category, CATEGORY_WEIGHT,
                weightedTotal, weightsUsed, reasons);

        weightedTotal = result[0];
        weightsUsed = result[1];

        result = addTextScore("Brand", lost.brand,
                found.brand, BRAND_WEIGHT,
                weightedTotal, weightsUsed, reasons);

        weightedTotal = result[0];
        weightsUsed = result[1];

        result = addTextScore("Colour", lost.colour,
                found.colour, COLOUR_WEIGHT,
                weightedTotal, weightsUsed, reasons);

        weightedTotal = result[0];
        weightsUsed = result[1];

        result = addTextScore("Location", lost.location,
                found.location, LOCATION_WEIGHT,
                weightedTotal, weightsUsed, reasons);

        weightedTotal = result[0];
        weightsUsed = result[1];

        if (lost.date != null && found.date != null) {

            long daysApart = Math.abs(
                    ChronoUnit.DAYS.between(lost.date, found.date));

            // Similarity decreases to zero when dates are 30 or more days apart.
            double dateScore = Math.max(0.0, 1.0 - daysApart / 30.0);

            weightedTotal += dateScore * DATE_WEIGHT;
            weightsUsed += DATE_WEIGHT;

            String closeness = dateScore >= 0.75 ? "close" : "different";
            String dayWord = daysApart == 1 ? "day" : "days";

            reasons.add("Date: " + closeness + " ("
                    + daysApart + " " + dayWord + " apart)");
        }

        double percentage = weightsUsed == 0.0
                ? 0.0
                : (weightedTotal / weightsUsed) * 100.0;

        return new MatchResult(percentage, reasons);
    }

    private static double[] addTextScore(
            String label,
            String lostValue,
            String foundValue,
            double weight,
            double weightedTotal,
            double weightsUsed,
            List<String> reasons) {

        // Skip an attribute if either report does not contain it.
        if (isBlank(lostValue) || isBlank(foundValue)) {
            return new double[]{weightedTotal, weightsUsed};
        }

        double score = textSimilarity(lostValue, foundValue);

        weightedTotal += score * weight;
        weightsUsed += weight;

        String comparison = score >= 0.75 ? "similar" : "different";
        reasons.add(label + ": " + comparison);

        return new double[]{weightedTotal, weightsUsed};
    }

    private static double textSimilarity(String first, String second) {

        String a = normalize(first);
        String b = normalize(second);

        if (a.equals(b)) {
            return 1.0;
        }

        int distance = levenshteinDistance(a, b);
        int longestLength = Math.max(a.length(), b.length());

        if (longestLength == 0) {
            return 1.0;
        }

        return 1.0 - (double) distance / longestLength;
    }

    private static String normalize(String text) {

        return Normalizer.normalize(text.toLowerCase().trim(),
                        Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ");
    }

    private static int levenshteinDistance(String first, String second) {

        int[][] distance =
                new int[first.length() + 1][second.length() + 1];

        for (int i = 0; i <= first.length(); i++) {
            distance[i][0] = i;
        }

        for (int j = 0; j <= second.length(); j++) {
            distance[0][j] = j;
        }

        for (int i = 1; i <= first.length(); i++) {

            for (int j = 1; j <= second.length(); j++) {

                int cost = first.charAt(i - 1) == second.charAt(j - 1)
                        ? 0 : 1;

                distance[i][j] = Math.min(
                        Math.min(distance[i - 1][j] + 1,
                                distance[i][j - 1] + 1),
                        distance[i - 1][j - 1] + cost);
            }
        }

        return distance[first.length()][second.length()];
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static void main(String[] args) {

        ItemReport lostReport = new ItemReport(
                "Black HP laptop near library",
                "Electronics",
                "HP",
                "Black",
                "Library",
                LocalDate.of(2026, 11, 20));

        ItemReport foundReport = new ItemReport(
                "Black HP laptop near library",
                "Electronics",
                "HP",
                "Black",
                "Library",
                LocalDate.of(2026, 11, 21));

        MatchResult result = compare(lostReport, foundReport);

        System.out.printf("Match Percentage: %.2f%%%n", result.percentage);

        System.out.println("Match Reasons:");

        for (String reason : result.reasons) {
            System.out.println("- " + reason);
        }
    }
}

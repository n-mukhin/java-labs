class Journey {
    private boolean hasStarted;
    private boolean hasPrintedJourneyInfo = false;

    public Journey() {
        this.hasStarted = false;
        this.hasPrintedJourneyInfo = false;
    }

    public void startJourney(Dreamer dreamer) {
        startJourneyInternal(dreamer);
    }

    public void startJourneyInternal(Dreamer dreamer) {
        if (!hasPrintedJourneyInfo) {
            System.out.println("Размечтавшись, " + dreamer + " не заметил, как погрузился в сон. И во сне " + dreamer.getDream());
            hasPrintedJourneyInfo = true;

            if (!dreamer.hasGoneToSunCity()) {
                System.out.println("А наутро " + dreamer + " исчез. К завтраку он не явился, " +
                        "а когда коротышки пришли к нему в комнату, они увидели на столе записку, " +
                        "в которой было всего три слова: \"В Солнечный город\", и подпись: \"" + dreamer + "\". \n" +
                        "Прочитав записку, коротышки сразу поняли, что " + dreamer + " уехал в Солнечный город.");
            }
        }
    }
}








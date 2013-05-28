[
    new MagicPermanentActivation(
        [
            MagicCondition.ONE_CREATURE_CONDITION
        ],
        new MagicActivationHints(MagicTiming.Pump),
        "Pump"
    ) {
        
        @Override
        public MagicEvent[] getCostEvent(final MagicPermanent source) {
            return [
                new MagicSacrificePermanentEvent(
                    source,
                    MagicTargetChoice.SACRIFICE_CREATURE
                )
            ];
        }
        @Override
        public MagicEvent getPermanentEvent(final MagicPermanent source, final MagicPayedCost payedCost) {
            final boolean isHuman = payedCost.getTarget().hasSubType(MagicSubType.Human);
            final String message = 
                "SN is indestructible this turn." + 
                (isHuman ? " Put a +1/+1 counter on SN." : "");
            return new MagicEvent(
                source,
                isHuman ? 1 : 0,
                this,
                message
            );
        }
        @Override
        public void executeEvent(final MagicGame game, final MagicEvent event) {
            game.doAction(new MagicSetAbilityAction(
                event.getPermanent(),
                MagicAbility.Indestructible
            ));
            game.doAction(new MagicChangeCountersAction(
                event.getPermanent(),
                MagicCounterType.PlusOne,
                event.getRefInt(),
                true
            ));
        }
    }
]

-- ============================================
-- TONTI Backend - V2
-- Stripe Checkout (page hébergée), abstraction PSP, suppression des cartes enregistrées
-- ============================================

ALTER TABLE payments ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'STRIPE';
ALTER TABLE payments ADD COLUMN provider_order_id VARCHAR(100);
ALTER TABLE payments ADD COLUMN provider_transaction_id VARCHAR(100);
ALTER TABLE payments ADD COLUMN channel VARCHAR(10) NOT NULL DEFAULT 'WEB';

-- Paiements historiques (PaymentIntents) : la référence de commande est le PaymentIntent
UPDATE payments SET provider_order_id = stripe_payment_intent_id, provider_transaction_id = stripe_payment_intent_id
    WHERE stripe_payment_intent_id IS NOT NULL;

ALTER TABLE payments ADD CONSTRAINT uk_payments_provider_order_id UNIQUE (provider_order_id);

-- Un seul paiement réussi par membre et par round (garde-fou contre les doubles débits)
CREATE UNIQUE INDEX uk_payments_round_user_succeeded
    ON payments(round_id, user_id)
    WHERE statut = 'SUCCEEDED';

-- Les cartes ne sont plus stockées côté Tonti : la saisie se fait sur la page hébergée du PSP (PCI-DSS SAQ-A)
DROP TABLE IF EXISTS payment_methods;

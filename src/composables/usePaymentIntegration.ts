import type { PaymentInfo, PaymentMethod } from '@/types';

export function usePaymentIntegration() {
  /**
   * Générer un lien de paiement Stripe
   */
  function generateStripeLink(amount: number, currency: string, recipientName: string): string {
    // En production, ceci devrait être un vrai lien Stripe Payment Link
    // Pour l'instant, on retourne un lien de démo
    return `https://buy.stripe.com/test_demo?amount=${amount}&currency=${currency}&recipient=${encodeURIComponent(recipientName)}`;
  }

  /**
   * Générer un lien PayPal.me
   */
  function generatePayPalLink(email: string, amount: number, currency: string): string {
    const username = email.split('@')[0];
    return `https://paypal.me/${username}/${amount}${currency}`;
  }

  /**
   * Générer un QR code data pour une adresse crypto
   */
  function generateCryptoQRData(network: string, address: string, amount?: number): string {
    if (network === 'BTC') {
      return amount ? `bitcoin:${address}?amount=${amount}` : `bitcoin:${address}`;
    }
    if (network === 'ETH' || network === 'USDT') {
      return amount ? `ethereum:${address}?value=${amount}` : `ethereum:${address}`;
    }
    return address;
  }

  /**
   * Générer un lien de paiement mobile money (pour le Maroc)
   */
  function generateMobileMoneyLink(phoneNumber: string, amount: number): string {
    // Pour le Maroc : formats populaires comme CashPlus, Wafacash, etc.
    return `tel:${phoneNumber}`;
  }

  /**
   * Formater les informations de paiement pour l'affichage
   */
  function formatPaymentInfo(paymentInfo: PaymentInfo): string {
    switch (paymentInfo.method) {
      case 'stripe':
        return paymentInfo.details.stripeLink || 'Lien Stripe non configuré';
      case 'paypal':
        return paymentInfo.details.paypalEmail || 'Email PayPal non configuré';
      case 'crypto':
        if (paymentInfo.details.cryptoAddress) {
          const { network, address } = paymentInfo.details.cryptoAddress;
          return `${network}: ${address.substring(0, 10)}...${address.substring(address.length - 6)}`;
        }
        return 'Adresse crypto non configurée';
      case 'mobile_money':
        return paymentInfo.details.phoneNumber || 'Numéro non configuré';
      case 'bank_transfer':
        return paymentInfo.details.iban
          ? `IBAN: ${paymentInfo.details.iban.substring(0, 10)}...`
          : 'IBAN non configuré';
      case 'cash':
        return 'Paiement en espèces';
      default:
        return 'Méthode non configurée';
    }
  }

  /**
   * Obtenir l'icône pour une méthode de paiement
   */
  function getPaymentMethodIcon(method: PaymentMethod): string {
    switch (method) {
      case 'stripe':
        return '💳';
      case 'paypal':
        return '🅿️';
      case 'crypto':
        return '₿';
      case 'mobile_money':
        return '📱';
      case 'bank_transfer':
        return '🏦';
      case 'cash':
        return '💵';
      default:
        return '💰';
    }
  }

  /**
   * Copier les informations de paiement dans le presse-papiers
   */
  async function copyPaymentInfo(paymentInfo: PaymentInfo): Promise<boolean> {
    try {
      let textToCopy = '';

      switch (paymentInfo.method) {
        case 'stripe':
          textToCopy = paymentInfo.details.stripeLink || '';
          break;
        case 'paypal':
          textToCopy = paymentInfo.details.paypalEmail || '';
          break;
        case 'crypto':
          textToCopy = paymentInfo.details.cryptoAddress?.address || '';
          break;
        case 'mobile_money':
          textToCopy = paymentInfo.details.phoneNumber || '';
          break;
        case 'bank_transfer':
          textToCopy = paymentInfo.details.iban || '';
          break;
        default:
          return false;
      }

      if (textToCopy) {
        await navigator.clipboard.writeText(textToCopy);
        return true;
      }
      return false;
    } catch (error) {
      console.error('Failed to copy payment info:', error);
      return false;
    }
  }

  /**
   * Vérifier si une méthode de paiement est configurée
   */
  function isPaymentMethodConfigured(paymentInfo?: PaymentInfo): boolean {
    if (!paymentInfo) return false;

    switch (paymentInfo.method) {
      case 'stripe':
        return !!paymentInfo.details.stripeLink;
      case 'paypal':
        return !!paymentInfo.details.paypalEmail;
      case 'crypto':
        return !!paymentInfo.details.cryptoAddress?.address;
      case 'mobile_money':
        return !!paymentInfo.details.phoneNumber;
      case 'bank_transfer':
        return !!paymentInfo.details.iban;
      case 'cash':
        return true; // Cash is always "configured"
      default:
        return false;
    }
  }

  return {
    generateStripeLink,
    generatePayPalLink,
    generateCryptoQRData,
    generateMobileMoneyLink,
    formatPaymentInfo,
    getPaymentMethodIcon,
    copyPaymentInfo,
    isPaymentMethodConfigured,
  };
}

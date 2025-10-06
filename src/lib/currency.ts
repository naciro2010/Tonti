export const MAD_FORMATTER = new Intl.NumberFormat("fr-MA", {
  style: "currency",
  currency: "MAD",
  maximumFractionDigits: 0
});

export function formatMAD(value: number): string {
  return MAD_FORMATTER.format(value);
}

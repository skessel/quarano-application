export class DateFunctions {
  public static addDays(date: Date, numberOfDays: number): Date {
    const dateClone = new Date(date.valueOf());
    dateClone.setDate(dateClone.getDate() + numberOfDays);
    return dateClone;
  }

  public static getDateWithoutTime(date: Date): string {
    const dateClone = new Date(date.valueOf());
    return new Date(dateClone.getTime() - dateClone.getTimezoneOffset() * 60000).toISOString().split('T')[0];
  }

  public static toCustomLocaleDateString(date: Date): string {
    const dateClone = new Date(date.valueOf());
    return dateClone.toLocaleDateString('de-DE', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  public static isDateInPast = function (date: Date): boolean {
    const dateClone = new Date(date.valueOf());
    const today = new Date(new Date().getFullYear(), new Date().getMonth(), new Date().getDate());
    return this.getDateWithoutTime(today) > this.getDateWithoutTime(dateClone);
  };
}

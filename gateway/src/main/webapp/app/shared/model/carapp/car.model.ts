export interface ICar {
  id?: number;
  make?: string | null;
  model?: string | null;
  price?: number | null;
}

export const defaultValue: Readonly<ICar> = {};

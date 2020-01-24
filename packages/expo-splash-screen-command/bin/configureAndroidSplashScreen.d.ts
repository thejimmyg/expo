import { Mode } from './constants';
export default function configureAndroidSplashScreen({ imagePath, mode, backgroundColor, }: {
    imagePath?: string;
    mode: Mode;
    backgroundColor: string;
}): Promise<void>;

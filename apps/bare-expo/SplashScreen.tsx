import * as React from 'react';
import { StyleSheet, Image, View, Text, Dimensions, StatusBar } from 'react-native';
import * as SplashScreen from 'expo-splash-screen';

const SplashScreenGif = require('./assets/splashscreen-triangle-red.png');

interface State {
  isShowingJSSplashScreen: boolean;
  isShowingNativeSplashScreen: boolean;
}

StatusBar.setTranslucent(true);

console.log('SplashScreen.preventAutoHideAsync called');
SplashScreen.preventAutoHideAsync()
  .then(() => console.log('SplashScreen.preventAutoHideAsync returned'))
  .catch(error => console.log(`SplashScreen.preventAutoHideAsync error: ${error}`));

export default class App extends React.Component<{}, State> {
  readonly state: State = {
    isShowingJSSplashScreen: false,
    isShowingNativeSplashScreen: true,
  };

  async componentDidMount() {
    setTimeout(() => {
      console.log('JS SplashScreen phase - starting');
      this.setState({
        isShowingNativeSplashScreen: false,
        isShowingJSSplashScreen: true,
      });
    }, 300);
  }

  hideNativeSplashScreen = async () => {
    console.log('SplashScreen.hideAsync called');
    SplashScreen.hideAsync()
      .then(() => console.log('SplashScreen.hideAsync returned'))
      .catch(error => console.log(`SplashScreen.hideAsync error: ${error}`));

    setTimeout(async () => {
      console.log('JS SplashScreen phase - ending');
      this.setState({ isShowingJSSplashScreen: false });
    }, 2000);
  };

  render() {
    if (this.state.isShowingNativeSplashScreen) {
      return <View style={styles.container} />;
    }

    if (this.state.isShowingJSSplashScreen) {
      return (
        <View style={styles.container}>
          <Image
            style={styles.splashscreen}
            source={SplashScreenGif}
            resizeMode="contain"
            fadeDuration={0}
            onLoadEnd={this.hideNativeSplashScreen}
          />
        </View>
      );
    }

    return (
      <View style={styles.container}>
        <Text style={styles.text}>Hello SplashScreen demo!</Text>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#aabbcc',
  },
  text: {
    color: 'white',
    fontWeight: 'bold',
  },
  splashscreen: {
    width: Dimensions.get('window').width,
    height: Dimensions.get('window').height,
  },
});

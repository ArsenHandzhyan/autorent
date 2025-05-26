import React from 'react';
import { Layout, Menu } from 'antd';
import { UserOutlined, CarOutlined, WalletOutlined, HistoryOutlined } from '@ant-design/icons';
import UserInfo from './UserInfo';
import UserRentals from './UserRentals';
import UserAccount from './UserAccount';
import UserHistory from './UserHistory';

const { Content, Sider } = Layout;

const UserProfile = () => {
    const [selectedKey, setSelectedKey] = React.useState('info');

    const renderContent = () => {
        switch (selectedKey) {
            case 'info':
                return <UserInfo />;
            case 'rentals':
                return <UserRentals />;
            case 'account':
                return <UserAccount />;
            case 'history':
                return <UserHistory />;
            default:
                return <UserInfo />;
        }
    };

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Sider width={200} theme="light">
                <Menu
                    mode="inline"
                    selectedKeys={[selectedKey]}
                    style={{ height: '100%', borderRight: 0 }}
                    onSelect={({ key }) => setSelectedKey(key)}
                >
                    <Menu.Item key="info" icon={<UserOutlined />}>
                        Личные данные
                    </Menu.Item>
                    <Menu.Item key="rentals" icon={<CarOutlined />}>
                        Мои аренды
                    </Menu.Item>
                    <Menu.Item key="account" icon={<WalletOutlined />}>
                        Мой счет
                    </Menu.Item>
                    <Menu.Item key="history" icon={<HistoryOutlined />}>
                        История
                    </Menu.Item>
                </Menu>
            </Sider>
            <Layout style={{ padding: '24px' }}>
                <Content style={{ background: '#fff', padding: 24, margin: 0, minHeight: 280 }}>
                    {renderContent()}
                </Content>
            </Layout>
        </Layout>
    );
};

export default UserProfile; 